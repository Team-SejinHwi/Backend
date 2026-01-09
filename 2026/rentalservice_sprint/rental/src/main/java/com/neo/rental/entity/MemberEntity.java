package com.neo.rental.entity;

import com.neo.rental.dto.MemberDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "member_table")
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    private Long id;

    @Column(unique = true) // unique = true --> email 중복 허용하지 않음.
    private String memberEmail;

    @Column
    private String memberPassword;

    public static MemberEntity toMemberEntity(MemberDTO memberDTO) {
        // 새로운 MemberEntity 객체 생성
        MemberEntity memberEntity = new MemberEntity();

        // DTO에서 멤버 이메일, 패스워드, 이름을 추출하여 MemberEntity에 설정
        memberEntity.setMemberEmail(memberDTO.getMemberEmail());
        memberEntity.setMemberPassword(memberDTO.getMemberPassword());

        return memberEntity;
    }
}
