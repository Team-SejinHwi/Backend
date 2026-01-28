package com.neo.rental.entity;

import com.neo.rental.dto.MemberDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "member_table")
@EntityListeners(AuditingEntityListener.class) // [중요] 생성/수정 시간 자동 감지 리스너
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // memberEmail -> email 로 변경
    @Column(unique = true, nullable = false)
    private String email;

    // memberPassword -> password 로 변경
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column
    private String address;

    // --- 시간 기록 필드 ---

    @CreatedDate
    @Column(updatable = false) // 생성 시간은 수정 불가
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;

    // [추가] 리프레시 토큰 저장용 필드
    @Column(length = 1000) // 토큰 길이가 길 수 있으므로 넉넉하게
    private String refreshToken;

    // [추가] 리프레시 토큰 업데이트 메서드
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // DTO -> Entity 변환 메서드
    public static MemberEntity toMemberEntity(MemberDTO memberDTO) {
        MemberEntity memberEntity = new MemberEntity();

        memberEntity.setEmail(memberDTO.getEmail());
        memberEntity.setPassword(memberDTO.getPassword());
        memberEntity.setName(memberDTO.getName());
        memberEntity.setPhone(memberDTO.getPhone());
        memberEntity.setAddress(memberDTO.getAddress());

        return memberEntity;
    }

    // 정보 수정 메소드
    public void updateMember(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    // 비밀번호 변경 메소드
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}