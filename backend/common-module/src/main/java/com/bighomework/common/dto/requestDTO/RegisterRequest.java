package com.bighomework.common.dto.requestDTO;

import com.bighomework.common.enums.MembershipLevel;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少为6位")
    private String password;
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;
    
    @NotBlank(message = "身份证号不能为空")
    @Size(min = 18, max = 18, message = "身份证号必须为18位")
    private String idCard;

    @NotNull(message = "必须选择一个会员等级")
    private MembershipLevel membershipLevel;
}