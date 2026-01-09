package com.neo.rental.dto;


import com.neo.rental.entity.MemberEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class MemberDTO {
    private Long id;
    private String memberEmail;
    private String memberPassword;

    // (Entity -> DTO 변환)
    public static MemberDTO toMemberDTO(MemberEntity memberEntity) {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setId(memberEntity.getId());
        memberDTO.setMemberEmail(memberEntity.getMemberEmail());
        memberDTO.setMemberPassword(memberEntity.getMemberPassword()); // 주석처리 해야함. 혹은 response dto를 따로 만들어야한다.
        return memberDTO;
    }
}
