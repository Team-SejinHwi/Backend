package com.neo.rental.service;

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

import java.time.temporal.ChronoUnit; // ★ 시간 계산을 위해 추가됨
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;

    // 1. 대여 신청 (WAITING 상태로 생성 + ★총 가격 계산 추가)
    public RentalResponseDto createRental(String renterEmail, RentalRequestDto dto) {
        MemberEntity renter = memberRepository.findByEmail(renterEmail)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        ItemEntity item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        // [검증] 내 물건 대여 불가
        if (item.getMember().getId().equals(renter.getId())) {
            throw new IllegalStateException("자신의 물건은 대여할 수 없습니다.");
        }

        // ★ [추가] 시간 차이 및 총 가격 계산 로직 시작
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("종료 시간이 시작 시간보다 빠를 수 없습니다.");
        }

        // 시간 차이 구하기 (단위: 시간)
        long hours = ChronoUnit.HOURS.between(dto.getStartDate(), dto.getEndDate());

        // 최소 1시간 요금 적용 (0시간으로 계산되면 1시간으로 변경)
        if (hours < 1) {
            hours = 1;
        }

        // 총 금액 = 시간 * 시간당 가격
        int totalPrice = (int) (hours * item.getPrice());
        // ★ [추가] 계산 끝

        RentalEntity rental = RentalEntity.builder()
                .item(item)
                .renter(renter)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(RentalStatus.WAITING) // 초기 상태 WAITING
                .totalPrice(totalPrice)       // ★ 계산된 총 금액 저장
                .build();

        RentalEntity savedRental = rentalRepository.save(rental);
        return new RentalResponseDto(savedRental);
    }

    // 2. 내 대여 내역 조회 (구매자용)
    @Transactional(readOnly = true)
    public List<RentalResponseDto> getMyRentals(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        return rentalRepository.findByRenterIdOrderByCreatedAtDesc(member.getId()).stream()
                .map(RentalResponseDto::new)
                .collect(Collectors.toList());
    }

    // 3. 받은 대여 요청 조회 (판매자용)
    @Transactional(readOnly = true)
    public List<RentalResponseDto> getReceivedRequests(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        return rentalRepository.findByItem_Member_IdOrderByCreatedAtDesc(member.getId()).stream()
                .map(RentalResponseDto::new)
                .collect(Collectors.toList());
    }

    // 변경 - 승인 및 거절 통합
    // 4. 승인/거절 처리 (통합 로직)
    public RentalResponseDto handleDecision(Long rentalId, String ownerEmail, RentalDecisionDto dto) {
        RentalEntity rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보를 찾을 수 없습니다."));

        // 1. 주인 확인
        if (!rental.getItem().getMember().getEmail().equals(ownerEmail)) {
            throw new IllegalStateException("해당 물건의 주인이 아닙니다.");
        }

        // 2. 이미 처리된 건인지 확인 (대기 중일 때만 처리 가능)
        if (rental.getStatus() != RentalStatus.WAITING) {
            throw new IllegalStateException("이미 처리되었거나 취소된 건입니다.");
        }

        // 3. 승인 vs 거절 로직 분기
        if (dto.isApproved()) {
            // [승인 로직]
            rental.setStatus(RentalStatus.APPROVED);
            rental.setRejectReason(null); // 승인이므로 거절 사유 초기화
        } else {
            // [거절 로직]
            // ★ 거절 사유 필수 체크 (만들어둔 필드 활용)
            if (dto.getRejectReason() == null || dto.getRejectReason().trim().isEmpty()) {
                throw new IllegalArgumentException("거절 시에는 거절 사유를 반드시 입력해야 합니다.");
            }

            rental.setStatus(RentalStatus.REJECTED); // REJECTED 상태 저장
            rental.setRejectReason(dto.getRejectReason()); // 사유 저장
        }

        return new RentalResponseDto(rental);
    }

    // 5. 취소 처리 (신청자만 가능)
    public RentalResponseDto cancelRental(Long rentalId, String renterEmail) {
        RentalEntity rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보를 찾을 수 없습니다."));

        // [권한] 신청자 확인
        if (!rental.getRenter().getEmail().equals(renterEmail)) {
            throw new IllegalStateException("본인의 신청 내역만 취소할 수 있습니다.");
        }

        if (rental.getStatus() == RentalStatus.RENTING || rental.getStatus() == RentalStatus.RETURNED) {
            throw new IllegalStateException("이미 대여가 시작되었거나 완료된 건은 취소할 수 없습니다.");
        }

        rental.setStatus(RentalStatus.CANCELED);
        return new RentalResponseDto(rental);
    }
}