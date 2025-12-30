package com.bighomework.admin.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "system_messages")
public class SystemMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long msgId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String publisher;

    private LocalDateTime createTime;

    private Boolean isPublic;
}