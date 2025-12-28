package com.bighomework.auth.service;

import com.bighomework.auth.entity.User;
import com.bighomework.common.dto.requestDTO.RegisterRequest;
import com.bighomework.common.dto.requestDTO.UpdatePasswordRequest;
import com.bighomework.common.dto.requestDTO.UpdateProfileRequest;

public interface AuthService {

    User registerUser(RegisterRequest request);

    void updateProfile(String phone, UpdateProfileRequest request);

    void updatePassword(String phone, UpdatePasswordRequest request);
}