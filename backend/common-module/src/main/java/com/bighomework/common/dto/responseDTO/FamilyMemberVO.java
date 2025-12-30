package com.bighomework.common.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 常用乘机人（家人）视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMemberVO {

    private Integer memberId;

    private String name;

    private String phone;

    private String idCard;
}