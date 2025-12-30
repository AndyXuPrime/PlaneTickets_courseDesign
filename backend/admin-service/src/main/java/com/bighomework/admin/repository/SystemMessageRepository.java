package com.bighomework.admin.repository;

import com.bighomework.admin.entity.SystemMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemMessageRepository extends JpaRepository<SystemMessage, Long> {

    /**
     * 查询所有已发布的公共消息，并按发布时间倒序排列 (最新的在最上面)
     * SQL: SELECT * FROM system_messages WHERE is_public = true ORDER BY create_time DESC
     */
    List<SystemMessage> findByIsPublicTrueOrderByCreateTimeDesc();
}