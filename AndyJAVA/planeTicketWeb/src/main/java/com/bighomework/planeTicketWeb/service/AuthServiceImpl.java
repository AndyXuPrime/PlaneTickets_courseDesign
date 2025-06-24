package com.bighomework.planeTicketWeb.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bighomework.planeTicketWeb.dto.requestDTO.RegisterRequest;
import com.bighomework.planeTicketWeb.entity.Customer;
import com.bighomework.planeTicketWeb.enums.Gender;
import com.bighomework.planeTicketWeb.exception.BusinessException; // 导入 PasswordEncoder
import com.bighomework.planeTicketWeb.repository.CustomerRepository;

import lombok.RequiredArgsConstructor; // 建议为写操作添加事务注解

@Service
@RequiredArgsConstructor // Lombok 会为所有 final 字段生成构造函数，实现依赖注入
public class AuthServiceImpl implements AuthService {

    private final CustomerRepository customerRepository;
    
    /**
     * 【核心新增】
     * 注入由 Spring Security 提供的 PasswordEncoder Bean。
     * 这是处理密码的标准、安全做法。
     */
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional // 整个注册过程应该是一个事务
    public Customer registerUser(RegisterRequest request) {
        // 1. 检查手机号是否已存在
        customerRepository.findByPhone(request.getPhone()).ifPresent(c -> {
            throw new BusinessException("该手机号已被注册");
        });
        
        // 2. 检查身份证号是否已存在
        customerRepository.findByIdCard(request.getIdCard()).ifPresent(c -> {
            throw new BusinessException("该身份证号已被注册");
        });

        Customer customer = new Customer();
        customer.setName(request.getName());
        
        // 【核心修改】使用 passwordEncoder 来处理密码。
        // 即使当前配置是 NoOpPasswordEncoder（不加密），这也是正确的写法。
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setIdCard(request.getIdCard());
        customer.setMembershipLevel(request.getMembershipLevel());

        // 3. 根据身份证号自动设置性别
        try {
            customer.setGender(getGenderFromIdCard(request.getIdCard()));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的身份证号码，无法完成注册");
        }
        
        // 4. 保存到数据库
        return customerRepository.save(customer);
    }

    /**
     * 私有辅助方法：从18位身份证号中提取性别。
     */
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