package com.bighomework.auth.service.impl;

import com.bighomework.auth.entity.FamilyMember;
import com.bighomework.auth.entity.User;
import com.bighomework.auth.repository.FamilyMemberRepository;
import com.bighomework.auth.repository.UserRepository;
import com.bighomework.auth.service.AuthService;
import com.bighomework.common.dto.requestDTO.FamilyMemberRequest;
import com.bighomework.common.dto.requestDTO.RegisterRequest;
import com.bighomework.common.dto.requestDTO.UpdatePasswordRequest;
import com.bighomework.common.dto.requestDTO.UpdateProfileRequest;
import com.bighomework.common.enums.Gender;
import com.bighomework.common.enums.MembershipLevel;
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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final FamilyMemberRepository familyMemberRepository;
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
            log.info("新航司管理员入驻申请: {}", request.getPhone());
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

    @Override
    public List<FamilyMember> getFamilyMembers(String phone) {

        if (phone == null || phone.isEmpty()) {
            log.error("getFamilyMembers 参数 phone 为空");
            return new ArrayList<>();
        }

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        List<FamilyMember> list = familyMemberRepository.findByUserId(user.getUserId());
// 4. 【关键】打印日志，看看数据库到底查到了几个人，ID 是多少
        if (list != null) {
            list.forEach(m -> log.info("AuthService find member: ID={}, Name={}", m.getMemberId(), m.getName()));
        }
        return list != null ? list : new ArrayList<>();
    }

    @Transactional
    @Override
    public void addFamilyMember(String phone, FamilyMemberRequest req) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        FamilyMember member = new FamilyMember();
        member.setUserId(user.getUserId());
        member.setName(req.getName());
        member.setPhone(req.getPhone());
        try {
            member.setIdCard(EncryptionUtils.encrypt(req.getIdCard()));
        } catch (Exception e) {
            throw new BusinessException("身份证加密失败");
        }
        familyMemberRepository.save(member);
    }

    @Transactional
    @Override
    public void deleteFamilyMember(Integer memberId, String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        FamilyMember member = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("乘机人记录不存在"));

        if (!member.getUserId().equals(user.getUserId())) {
            throw new BusinessException("无权操作此记录");
        }
        familyMemberRepository.delete(member);
    }

    private Gender getGenderFromIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            throw new BusinessException("身份证格式非法");
        }
        int genderCode = Character.getNumericValue(idCard.charAt(16));
        return (genderCode % 2 != 0) ? Gender.男 : Gender.女;
    }

    @Override
    @Transactional
    public void updateMembership(Integer userId, MembershipLevel level) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
        user.setMembershipLevel(level);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
        // 默认重置为 123456
        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);
    }
}