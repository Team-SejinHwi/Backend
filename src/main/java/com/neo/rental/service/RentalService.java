package com.neo.rental.service;

import com.neo.rental.constant.ItemStatus; // [필수 Import]
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

    // 4. 승인/거절 (유지)
    public RentalResponseDto handleDecision(Long rentalId, String ownerEmail, RentalDecisionDto dto) {
        RentalEntity rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보 없음"));

        if (!rental.getItem().getMember().getEmail().equals(ownerEmail)) {
            throw new IllegalStateException("주인만 처리 가능합니다.");
        }
        if (rental.getStatus() != RentalStatus.WAITING) {
            throw new IllegalStateException("이미 처리된 건입니다.");
        }

        if (dto.isApproved()) {
            rental.setStatus(RentalStatus.APPROVED);
            rental.setRejectReason(null);
        } else {
            if (dto.getRejectReason() == null || dto.getRejectReason().trim().isEmpty()) {
                throw new IllegalArgumentException("거절 사유 필수");
            }
            rental.setStatus(RentalStatus.REJECTED);
            rental.setRejectReason(dto.getRejectReason());
        }
        return new RentalResponseDto(rental);
    }

    // 5. 취소 (유지)
    public RentalResponseDto cancelRental(Long rentalId, String renterEmail) {
        RentalEntity rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보 없음"));

        if (!rental.getRenter().getEmail().equals(renterEmail)) {
            throw new IllegalStateException("본인만 취소 가능");
        }
        if (rental.getStatus() == RentalStatus.RENTING || rental.getStatus() == RentalStatus.RETURNED) {
            throw new IllegalStateException("이미 진행/완료된 건은 취소 불가");
        }
        rental.setStatus(RentalStatus.CANCELED);
        return new RentalResponseDto(rental);
    }

    // 👇 [6. 추가] 반납 처리 (주인 또는 대여자가 실행)
    public RentalResponseDto completeReturn(Long rentalId, String email) {
        RentalEntity rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("대여 기록 없음"));

        // 권한 체크: 주인(Owner) 또는 빌린사람(Renter) 모두 반납 처리 가능하도록 허용
        boolean isOwner = rental.getItem().getMember().getEmail().equals(email);
        boolean isRenter = rental.getRenter().getEmail().equals(email);

        if (!isOwner && !isRenter) {
            throw new IllegalStateException("반납 처리 권한이 없습니다.");
        }

        // 1. 상태를 RETURNED(반납 완료)로 변경
        rental.setStatus(RentalStatus.RETURNED);

        // 2. 아이템 상태를 AVAILABLE(대여 가능)로 복구 -> 다시 검색됨!
        rental.getItem().setItemStatus(ItemStatus.AVAILABLE);

        return new RentalResponseDto(rental);
    }
}