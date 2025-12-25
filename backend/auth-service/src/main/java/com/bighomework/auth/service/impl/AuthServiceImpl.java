package com.bighomework.auth.service.impl; // 1. 修正包名

import com.bighomework.auth.entity.User;
import com.bighomework.auth.repository.UserRepository;
import com.bighomework.auth.service.AuthService;
import com.bighomework.common.dto.requestDTO.RegisterRequest;
import com.bighomework.common.enums.Gender;
import com.bighomework.common.enums.UserRole;
import com.bighomework.common.enums.UserStatus; // 引用刚才新建的枚举
import com.bighomework.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 建议注入加密器

    @Override
    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new BusinessException("该手机号已被注册");
        }
        if (userRepository.findByIdCard(request.getIdCard()).isPresent()) {
            throw new BusinessException("该身份证号已被注册");
        }

        User user = new User();
        user.setName(request.getName());
        // 使用加密器加密密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setIdCard(request.getIdCard());

        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }

        user.setMembershipLevel(request.getMembershipLevel());
        user.setGender(getGenderFromIdCard(request.getIdCard()));

        // 设置默认值
        user.setRole(UserRole.ROLE_USER);
        user.setStatus(UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    private Gender getGenderFromIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            throw new IllegalArgumentException("身份证号格式错误");
        }
        char genderCode = idCard.charAt(16);
        return (Character.getNumericValue(genderCode) % 2 != 0) ? Gender.男 : Gender.女;
    }
}