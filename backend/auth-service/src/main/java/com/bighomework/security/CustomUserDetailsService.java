package com.bighomework.planeTicketWeb.security;

// 【新增】引入 List 和 SimpleGrantedAuthority
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bighomework.planeTicketWeb.entity.Customer;
import com.bighomework.planeTicketWeb.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    /**
     * 根据用户名（在这里是手机号）加载用户信息。
     * 这个方法是Spring Security进行认证的核心环节。
     *
     * @param username 前端登录时传入的用户名（手机号）。
     * @return 一个包含用户凭证和权限的 UserDetails 对象。
     * @throws UsernameNotFoundException 如果在数据库中找不到用户。
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 根据手机号从数据库查找 Customer 实体
        Customer customer = customerRepository.findByPhone(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        
        // 2. 【核心修改】为用户创建一个包含默认权限的列表。
        //    Spring Security 的角色通常以 "ROLE_" 作为前缀。
        //    SimpleGrantedAuthority 是 GrantedAuthority 接口的标准实现。
        //    这个权限列表是解决 403 Forbidden 错误的关键。
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // 3. 创建并返回 Spring Security 的 User 对象。
        //    这个对象包含了认证所需的核心信息：
        //    - 用户名 (customer.getPhone())
        //    - 密码 (customer.getPassword())
        //    - 权限列表 (authorities)
        return new User(customer.getPhone(), customer.getPassword(), authorities);
    }
}