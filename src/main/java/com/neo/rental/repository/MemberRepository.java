package com.neo.rental.repository;

import com.neo.rental.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    // 이메일로 회원 정보 조회 (select * from member_table where email = ?)

    // [기존] Optional<MemberEntity> findByMemberEmail(String memberEmail);
    // [변경] Entity 필드명이 email이므로 메서드 이름도 아래와 같이 변경.
    Optional<MemberEntity> findByEmail(String email);
}