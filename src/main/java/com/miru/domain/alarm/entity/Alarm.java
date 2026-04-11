package com.miru.domain.alarm.entity;

import com.miru.domain.user.entity.User;
import com.miru.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알람 엔티티
 *
 * <p>특정 이벤트 발생 시 수신자에게 전달하는 알람이다.
 * 현재 두 가지 상황에서 알람이 생성된다:
 * <ul>
 *   <li>COMMENT  - 내 게시글에 댓글이 달렸을 때 / 내 댓글에 대댓글이 달렸을 때</li>
 *   <li>INQUIRY  - 내 문의에 관리자 답변이 등록되었을 때</li>
 * </ul>
 * 본인이 유발한 이벤트(자신의 게시글에 자신이 댓글 등)는 알람을 생성하지 않는다.
 * 읽지 않은 알람만 목록으로 제공하며, read() 또는 readAll()로 읽음 처리한다.
 */
@Entity
@Getter
@Table(name = "alarms")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 스펙 준수
public class Alarm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 알람을 받는 유저 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receive_user_id", nullable = false)
    private User receiveUser;

    /** 알람을 발생시킨 유저 (댓글 작성자, 관리자 등) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /** 알람 클릭 시 이동할 URL (예: /boards/123) */
    @Column(nullable = false)
    private String targetUrl;

    /** 알람 내용 문구 (최대 50자) */
    @Column(nullable = false, length = 50)
    private String content;

    /** 알람 유형 (COMMENT / INQUIRY) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlarmType type;

    /**
     * 읽음 여부 (기본값: false)
     * 읽은 알람은 목록 조회에서 제외됨
     */
    @Column(nullable = false)
    private boolean isRead;


    /**
     * 알람 생성 빌더
     * isRead는 항상 false로 초기화됨
     */
    @Builder
    public Alarm(User receiveUser, User sender, String targetUrl, String content, AlarmType type) {
        this.receiveUser = receiveUser;
        this.sender = sender;
        this.targetUrl = targetUrl;
        this.content = content;
        this.type = type;
        this.isRead = false; // 생성 시 항상 미읽음
    }

    /**
     * 알람 읽음 처리
     * readOne() 또는 readAll()에서 호출
     */
    public void read() {
        this.isRead = true;
    }
}
