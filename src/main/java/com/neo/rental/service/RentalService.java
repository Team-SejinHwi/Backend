package com.neo.rental.service;

import com.neo.rental.constant.ItemStatus;
import com.neo.rental.constant.RentalStatus;
import com.neo.rental.dto.RentalDecisionDto;
import com.neo.rental.dto.RentalRequestDto;
import com.neo.rental.dto.RentalResponseDto;
import com.neo.rental.entity.ItemEntity;
import com.neo.rental.entity.MemberEntity;
import com.neo.rental.entity.RentalEntity;
import com.neo.rental.repository.ItemRepository;
import com.neo.rental.repository.MemberRepository;
import com.neo.rental.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;

    // 1. 대여 신청 (유지)
    public RentalResponseDto createRental(String renterEmail, RentalRequestDto dto) {
        MemberEntity renter = memberRepository.findByEmail(renterEmail)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));
        ItemEntity item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (item.getMember().getId().equals(renter.getId())) {
            throw new IllegalStateException("자신의 물건은 대여할 수 없습니다.");
        }

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("종료 시간이 시작 시간보다 빠를 수 없습니다.");
        }

        // 아이템이 이미 대여중인지 확인 (방어 로직)
        if (item.getItemStatus() == ItemStatus.RENTED) {
            throw new IllegalStateException("현재 대여 불가능한 상품입니다.");
        }

        long hours = ChronoUnit.HOURS.between(dto.getStartDate(), dto.getEndDate());
        if (hours < 1) hours = 1;
        int totalPrice = (int) (hours * item.getPrice());

        RentalEntity rental = RentalEntity.builder()
                .item(item)
                .renter(renter)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(RentalStatus.WAITING)
                .totalPrice(totalPrice)
                .build();

        return new RentalResponseDto(rentalRepository.save(rental));
    }

    // 2. 내 대여 내역 (유지)
    @Transactional(readOnly = true)
    public List<RentalResponseDto> getMyRentals(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));
        return rentalRepository.findByRenterIdOrderByCreatedAtDesc(member.getId()).stream()
                .map(RentalResponseDto::new)
                .collect(Collectors.toList());
    }

    // 3. 받은 요청 (유지)
    @Transactional(readOnly = true)
    public List<RentalResponseDto> getReceivedRequests(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));
        return rentalRepository.findByItem_Member_IdOrderByCreatedAtDesc(member.getId()).stream()
                .map(RentalResponseDto::new)
                .collect(Collectors.toList());
    }

    // 4. 승인/거절 (수정됨: 승인 시 결제 대기 상태로 변경)
    public RentalResponseDto handleDecision(Long rentalId, String ownerEmail, RentalDecisionDto dto) {
        RentalEntity rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보 없음"));

        // 주인 검증
        if (!rental.getItem().getMember().getEmail().equals(ownerEmail)) {
            throw new IllegalStateException("주인만 처리 가능합니다.");
        }
        // 중복 처리 방지
        if (rental.getStatus() != RentalStatus.WAITING) {
            throw new IllegalStateException("이미 처리된 건입니다.");
        }

        if (dto.isApproved()) {
            // [방어 로직] 승인 시점에 아이템이 이미 선점(RENTED)되었는지 확인
            if (rental.getItem().getItemStatus() == ItemStatus.RENTED) {
                throw new IllegalStateException("이미 다른 예약으로 인해 대여중인 상품입니다.");
            }

            // 1) 렌탈 상태: APPROVED (결제 대기)
            rental.setStatus(RentalStatus.APPROVED);
            rental.setRejectReason(null);

            // 2) 아이템 상태: RENTED (선점 처리 - 다른 사람이 검색 못하게)
            // 결제 대기 중에도 물건은 확보되어야 하므로 RENTED로 설정
            rental.getItem().setItemStatus(ItemStatus.RENTED);

        } else {
            // 거절 처리
            if (dto.getRejectReason() == null || dto.getRejectReason().trim().isEmpty()) {
                throw new IllegalArgumentException("거절 사유 필수");
            }
            rental.setStatus(RentalStatus.REJECTED);
            rental.setRejectReason(dto.getRejectReason());
            // 아이템 상태는 AVAILABLE 유지
        }

        return new RentalResponseDto(rental);
    }

    // [NEW] 5. 대여 시작 (인계 확인) - 주인이 호출
    public RentalResponseDto startRental(Long rentalId, String ownerEmail) {
        RentalEntity rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("대여 기록 없음"));

        // 주인 검증
        if (!rental.getItem().getMember().getEmail().equals(ownerEmail)) {
            throw new IllegalStateException("물건 주인만 대여를 시작할 수 있습니다.");
        }

        // 상태 검증: 결제가 완료된(PAID) 상태여야만 시작 가능
        if (rental.getStatus() != RentalStatus.PAID) {
            throw new IllegalStateException("결제가 완료되지 않았거나, 이미 진행중인 대여입니다.");
        }

        // 상태 변경: PAID -> RENTING (실제 사용 시작)
        rental.setStatus(RentalStatus.RENTING);

        return new RentalResponseDto(rental);
    }

    // 6. 반납 처리 (수정됨: RENTING 상태에서만 가능)
    public RentalResponseDto returnItem(Long rentalId, String email) {
        RentalEntity rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("대여 기록 없음"));

        boolean isOwner = rental.getItem().getMember().getEmail().equals(email);
        boolean isRenter = rental.getRenter().getEmail().equals(email);

        if (!isOwner && !isRenter) {
            throw new IllegalStateException("반납 권한이 없습니다.");
        }

        // [중요] 상태 체크: 실제 대여 중(RENTING)일 때만 반납 가능
        // APPROVED나 PAID 상태에서 취소는 'cancelRental'을 사용해야 함
        if (rental.getStatus() != RentalStatus.RENTING) {
            throw new IllegalStateException("대여 중(인계 완료)인 상태에서만 반납이 가능합니다.");
        }

        // 상태 변경: RETURNED
        rental.setStatus(RentalStatus.RETURNED);

        // 아이템 복구: AVAILABLE
        rental.getItem().setItemStatus(ItemStatus.AVAILABLE);

        return new RentalResponseDto(rental);
    }

    // 7. 취소 (유지)
    public RentalResponseDto cancelRental(Long rentalId, String renterEmail) {
        RentalEntity rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보 없음"));

        if (!rental.getRenter().getEmail().equals(renterEmail)) {
            throw new IllegalStateException("본인만 취소 가능");
        }

        // 이미 사용 시작(RENTING)했거나 반납(RETURNED)된 건은 취소 불가
        // WAITING, APPROVED, PAID 상태에서는 취소 가능 (단, PAID 취소 시 환불 로직 필요 - 여기선 생략)
        if (rental.getStatus() == RentalStatus.RENTING || rental.getStatus() == RentalStatus.RETURNED) {
            throw new IllegalStateException("이미 진행/완료된 건은 취소 불가");
        }

        // 취소 시 아이템 상태가 RENTED였다면 풀어줘야 함 (APPROVED 상태에서 취소했을 경우)
        if (rental.getItem().getItemStatus() == ItemStatus.RENTED) {
            rental.getItem().setItemStatus(ItemStatus.AVAILABLE);
        }

        rental.setStatus(RentalStatus.CANCELED);
        return new RentalResponseDto(rental);
    }
}