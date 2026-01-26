package com.neo.rental.service;

import com.neo.rental.constant.RentalStatus;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;

    // 1. 대여 신청 (WAITING 상태로 생성)
    public RentalResponseDto createRental(String renterEmail, RentalRequestDto dto) {
        MemberEntity renter = memberRepository.findByEmail(renterEmail)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        ItemEntity item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        // [검증] 내 물건 대여 불가
        if (item.getMember().getId().equals(renter.getId())) {
            throw new IllegalStateException("자신의 물건은 대여할 수 없습니다.");
        }

        RentalEntity rental = RentalEntity.builder()
                .item(item)
                .renter(renter)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(RentalStatus.WAITING) // ★초기 상태 WAITING
                .build();

        RentalEntity savedRental = rentalRepository.save(rental);
        return new RentalResponseDto(savedRental);
    }

    // 2. 내 대여 내역 조회 (구매자용)
    @Transactional(readOnly = true)
    public List<RentalResponseDto> getMyRentals(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        // 리포지토리 메서드: findByRenterId...
        return rentalRepository.findByRenterIdOrderByCreatedAtDesc(member.getId()).stream()
                .map(RentalResponseDto::new)
                .collect(Collectors.toList());
    }

    // 3. 받은 대여 요청 조회 (판매자용)
    @Transactional(readOnly = true)
    public List<RentalResponseDto> getReceivedRequests(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        // 리포지토리 메서드: findByItem_Member_Id...
        return rentalRepository.findByItem_Member_IdOrderByCreatedAtDesc(member.getId()).stream()
                .map(RentalResponseDto::new)
                .collect(Collectors.toList());
    }

    // 4. 승인/거절 처리 (주인만 가능)
    public RentalResponseDto handleDecision(Long rentalId, String ownerEmail, boolean isApproved) {
        RentalEntity rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보를 찾을 수 없습니다."));

        // [권한] 주인 확인
        if (!rental.getItem().getMember().getEmail().equals(ownerEmail)) {
            throw new IllegalStateException("해당 물건의 주인이 아닙니다.");
        }

        if (isApproved) {
            rental.setStatus(RentalStatus.APPROVED); // 승인 -> APPROVED
        } else {
            // 거절 시 REJECTED가 없으므로 CANCELED로 처리
            rental.setStatus(RentalStatus.CANCELED);
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

        // 이미 진행중(RENTING)이거나 반납(RETURNED)된 건 취소 불가
        if (rental.getStatus() == RentalStatus.RENTING || rental.getStatus() == RentalStatus.RETURNED) {
            throw new IllegalStateException("이미 대여가 시작되었거나 완료된 건은 취소할 수 없습니다.");
        }

        rental.setStatus(RentalStatus.CANCELED); // 취소 -> CANCELED
        return new RentalResponseDto(rental);
    }
}