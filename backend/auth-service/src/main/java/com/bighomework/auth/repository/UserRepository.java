package com.bighomework.auth.repository;

import com.bighomework.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByIdCard(String idCard);
    List<User> findByPhoneContaining(String phone); // 新增模糊查询

}