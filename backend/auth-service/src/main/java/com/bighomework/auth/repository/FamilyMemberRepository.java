package com.bighomework.auth.repository;

import com.bighomework.auth.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Integer> {
    List<FamilyMember> findByUserId(Integer userId);
}