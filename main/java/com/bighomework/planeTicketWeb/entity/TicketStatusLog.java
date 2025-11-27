package com.bighomework.planeTicketWeb.entity;

import com.bighomework.planeTicketWeb.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ticket_status_log")
public class TicketStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private TicketStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private TicketStatus newStatus;

    @Column(name = "change_time")
    private LocalDateTime changeTime;

    @Column(name = "changed_by", length = 50)
    private String changedBy;
}