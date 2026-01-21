package com.neo.rental.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PasswordUpdateDto {
    private String currentPassword; // 현재 비번 (검증용)
    private String newPassword;     // 바꿀 비번
}