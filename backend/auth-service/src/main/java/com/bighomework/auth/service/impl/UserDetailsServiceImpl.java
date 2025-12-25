package com.bighomework.auth.service.impl;

import com.bighomework.auth.entity.User;
import com.bighomework.auth.repository.UserRepository;
import com.bighomework.common.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查找用户
        User user = userRepository.findByPhone(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        // 2. 状态校验
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DisabledException("账户状态异常: " + user.getStatus());
        }

        // 3. 转换角色
        var authorities = List.of(new SimpleGrantedAuthority(user.getRole().name()));

        // 4. 返回 UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getPhone(),
                user.getPassword(),
                authorities
        );
    }
}