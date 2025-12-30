package com.bighomework.auth.service;

import com.bighomework.auth.entity.FamilyMember;
import com.bighomework.auth.entity.User;
import com.bighomework.common.dto.requestDTO.FamilyMemberRequest;
import com.bighomework.common.dto.requestDTO.RegisterRequest;
import com.bighomework.common.dto.requestDTO.UpdatePasswordRequest;
import com.bighomework.common.dto.requestDTO.UpdateProfileRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AuthService {

    User registerUser(RegisterRequest request);

    void updateProfile(String phone, UpdateProfileRequest request);

    void updatePassword(String phone, UpdatePasswordRequest request);

    List<FamilyMember> getFamilyMembers(String phone);

    @Transactional
    void addFamilyMember(String phone, FamilyMemberRequest req);

    @Transactional
    void deleteFamilyMember(Integer memberId, String phone);
}