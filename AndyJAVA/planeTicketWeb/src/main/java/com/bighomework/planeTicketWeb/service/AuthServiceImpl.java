package com.bighomework.planeTicketWeb.service;

import com.bighomework.planeTicketWeb.dto.requestDTO.RegisterRequest;
import com.bighomework.planeTicketWeb.entity.Customer;
import com.bighomework.planeTicketWeb.enums.Gender;
import com.bighomework.planeTicketWeb.exception.BusinessException;
import com.bighomework.planeTicketWeb.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // 【新增】引入 Spring 的工具类

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public Customer registerUser(RegisterRequest request) {
        customerRepository.findByPhone(request.getPhone()).ifPresent(c -> {
            throw new BusinessException("该手机号已被注册");
        });
        
        customerRepository.findByIdCard(request.getIdCard()).ifPresent(c -> {
            throw new BusinessException("该身份证号已被注册");
        });

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setPassword(request.getPassword());
        customer.setPhone(request.getPhone());
        
        // 【核心修复】处理 email 字段
        // 使用 StringUtils.hasText() 判断 email 是否为 null 或空字符串
        if (StringUtils.hasText(request.getEmail())) {
            customer.setEmail(request.getEmail());
        } else {
            // 如果是 null 或空字符串，则明确地将实体中的 email 设置为 null
            customer.setEmail(null); 
        }
        
        customer.setIdCard(request.getIdCard());
        customer.setMembershipLevel(request.getMembershipLevel());

        try {
            customer.setGender(getGenderFromIdCard(request.getIdCard()));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的身份证号码，无法完成注册");
        }
        
        return customerRepository.save(customer);
    }

    private Gender getGenderFromIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            throw new IllegalArgumentException("身份证号格式不正确");
        }
        char genderCode = idCard.charAt(16);
        if (!Character.isDigit(genderCode)) {
             throw new IllegalArgumentException("身份证号性别位格式不正确");
        }
        int genderNum = Character.getNumericValue(genderCode);
        
        return (genderNum % 2 != 0) ? Gender.男 : Gender.女;
    }
}