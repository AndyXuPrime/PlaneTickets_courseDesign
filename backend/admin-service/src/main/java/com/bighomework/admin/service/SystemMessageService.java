package com.bighomework.admin.service;

import com.bighomework.admin.entity.SystemMessage;
import com.bighomework.admin.repository.SystemMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemMessageService {

    private final SystemMessageRepository repository;

    /**
     * 发布一条全员可见的系统消息
     * @param title 标题
     * @param content 内容
     * @param publisher 发布人 (通常是"系统管理员")
     */
    @Transactional
    public void publish(String title, String content, String publisher) {
        SystemMessage msg = new SystemMessage();
        msg.setTitle(title);
        msg.setContent(content);
        msg.setPublisher(publisher);
        msg.setIsPublic(true); // 默认设为公开
        msg.setCreateTime(LocalDateTime.now());

        repository.save(msg);
    }

    /**
     * 获取所有公开消息 (按时间倒序)
     */
    public List<SystemMessage> getAllPublicMessages() {
        return repository.findByIsPublicTrueOrderByCreateTimeDesc();
    }
}