package com.neo.rental.repository;

import com.neo.rental.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    // 나중에 환불할 때 rentalId로 결제 정보 찾기 위해 필요
    Optional<PaymentEntity> findByRentalId(Long rentalId);
}