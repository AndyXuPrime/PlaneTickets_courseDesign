package com.bighomework.auth.service.impl;

import com.bighomework.auth.entity.User;
import com.bighomework.auth.repository.UserRepository;
import com.bighomework.auth.service.AuthService;
import com.bighomework.common.dto.requestDTO.RegisterRequest;
import com.bighomework.common.dto.requestDTO.UpdatePasswordRequest;
import com.bighomework.common.dto.requestDTO.UpdateProfileRequest;
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
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new BusinessException("该手机号已被注册");
        }

        String encryptedId;
        try {
            encryptedId = EncryptionUtils.encrypt(request.getIdCard());
        } catch (Exception e) {
            log.error("身份证加密失败", e);
            throw new BusinessException("安全系统异常");
        }

        if (userRepository.findByIdCard(encryptedId).isPresent()) {
            throw new BusinessException("该身份证号已被注册");
        }

        User user = new User();
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setIdCard(encryptedId);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail() : null);
        user.setMembershipLevel(request.getMembershipLevel());
        user.setGender(getGenderFromIdCard(request.getIdCard()));
        user.setAirlineCode(request.getAirlineCode());

        if (UserRole.ROLE_AIRLINE_ADMIN.equals(request.getRole())) {
            user.setRole(UserRole.ROLE_AIRLINE_ADMIN);
            user.setStatus(UserStatus.PENDING);
            log.info("航司管理员申请注册: {}", request.getPhone());
        } else {
            user.setRole(UserRole.ROLE_USER);
            user.setStatus(UserStatus.ACTIVE);
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateProfile(String phone, UpdateProfileRequest request) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        user.setEmail(request.getEmail());
        user.setAvatarUrl(request.getAvatarUrl());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updatePassword(String phone, UpdatePasswordRequest request) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private Gender getGenderFromIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            throw new BusinessException("身份证格式非法");
        }
        int genderCode = Character.getNumericValue(idCard.charAt(16));
        return (genderCode % 2 != 0) ? Gender.男 : Gender.女;
    }
}