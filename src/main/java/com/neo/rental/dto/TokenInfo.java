package com.neo.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TokenInfo {
    private String grantType;   // "Bearer"
    private String accessToken; // 실제 토큰
    private String refreshToken;
}