package com.neo.rental.dto;

import com.neo.rental.entity.MemberEntity;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberDTO {
    private Long id;
    private String email;    // memberEmail -> email
    private String password; // memberPassword -> password

    // 추가된 필드
    private String name;
    private String phone;
    private String address;

    // 시간 정보 (조회 시 필요하다면 추가)
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    // (Entity -> DTO 변환) : 주로 DB에서 데이터를 가져와서 사용자에게 보여줄 때 사용
    public static MemberDTO toMemberDTO(MemberEntity memberEntity) {
        MemberDTO memberDTO = new MemberDTO();

        memberDTO.setId(memberEntity.getId());
        memberDTO.setEmail(memberEntity.getEmail());
        memberDTO.setName(memberEntity.getName());
        memberDTO.setPhone(memberEntity.getPhone());
        memberDTO.setAddress(memberEntity.getAddress());
        // 시간 정보 세팅
        memberDTO.setCreatedAt(memberEntity.getCreatedAt());
        memberDTO.setModifiedAt(memberEntity.getModifiedAt());

        return memberDTO;
    }
}