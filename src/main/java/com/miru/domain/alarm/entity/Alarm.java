package com.miru.domain.alarm.entity;

import com.miru.domain.user.entity.User;
import com.miru.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "alarms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alarm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receive_user_id", nullable = false)
    private User receiveUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false)
    private String targetUrl;

    @Column(nullable = false, length = 50)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlarmType type;

    @Column(nullable = false)
    private boolean isRead;


    @Builder
    public Alarm(User receiveUser, User sender, String targetUrl, String content, AlarmType type) {
        this.receiveUser = receiveUser;
        this.sender = sender;
        this.targetUrl = targetUrl;
        this.content = content;
        this.type = type;
        this.isRead = false;
    }

    public void read() {
        this.isRead = true;
    }
}