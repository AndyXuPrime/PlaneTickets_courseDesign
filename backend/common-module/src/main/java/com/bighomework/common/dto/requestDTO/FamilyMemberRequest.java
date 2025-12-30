package com.bighomework.common.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FamilyMemberRequest {
    @NotBlank(message = "姓名不能为空")
    private String name;
    private String phone;
    @NotBlank(message = "身份证号不能为空")
    private String idCard;
}