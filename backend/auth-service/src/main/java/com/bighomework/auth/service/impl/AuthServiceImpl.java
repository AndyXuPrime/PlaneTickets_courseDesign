package com.bighomework.auth.service.impl;

import com.bighomework.auth.entity.User;
import com.bighomework.auth.repository.UserRepository;
import com.bighomework.auth.service.AuthService;
import com.bighomework.common.dto.requestDTO.RegisterRequest;
import com.bighomework.common.enums.Gender;
import com.bighomework.common.enums.UserRole;
import com.bighomework.common.enums.UserStatus;
import com.bighomework.common.exception.BusinessException;
import com.bighomework.common.util.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerUser(RegisterRequest request) {
        // 1. 手机号唯一性校验
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new BusinessException("该手机号已被注册");
        }

        // 2. 身份证加密及唯一性校验
        String encryptedId;
        try {
            encryptedId = EncryptionUtils.encrypt(request.getIdCard());
        } catch (Exception e) {
            log.error("身份证加密异常: ", e);
            throw new BusinessException("安全组件初始化失败");
        }

        // 注意：数据库存的是密文，所以查询重复也必须用密文
        if (userRepository.findByIdCard(encryptedId).isPresent()) {
            throw new BusinessException("该身份证号已被注册");
        }

        // 3. 构建用户对象
        User user = new User();
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setIdCard(encryptedId);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail() : null);
        user.setMembershipLevel(request.getMembershipLevel());
        user.setGender(getGenderFromIdCard(request.getIdCard()));

        // 4. 设置默认权限和状态
        user.setRole(UserRole.ROLE_USER);
        user.setStatus(UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    private Gender getGenderFromIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            throw new BusinessException("身份证号格式错误");
        }
        char genderCode = idCard.charAt(16);
        return (Character.getNumericValue(genderCode) % 2 != 0) ? Gender.男 : Gender.女;
    }
}