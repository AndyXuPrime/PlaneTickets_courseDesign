package com.bighomework.common.dto.requestDTO;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatarUrl;
}