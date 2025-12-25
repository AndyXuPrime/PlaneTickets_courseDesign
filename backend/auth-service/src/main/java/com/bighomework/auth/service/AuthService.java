package com.bighomework.auth.service; // 1. 修正包名

import com.bighomework.auth.entity.User; // 2. 改用 User
import com.bighomework.common.dto.requestDTO.RegisterRequest; // 3. 引用 Common

public interface AuthService {
    // 返回值改为 User
    User registerUser(RegisterRequest request);
}