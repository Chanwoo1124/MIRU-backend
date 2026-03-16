package com.miru.domain.inquiry.entity;

import com.miru.domain.user.entity.User;
import com.miru.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.miru.domain.inquiry.entity.InquiryStatus.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryBoard extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status;

    /** 문의 삭제 시 답변도 함께 삭제 */
    @OneToOne(mappedBy = "inquiryBoard", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private InquiryAnswer answer;

    @Builder
    public InquiryBoard(User user, String title, String content) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.status = WAITING;
    }

    /** 문의 상태를 완료로 변경 (관리자 답변 등록 시 호출) */
    public void complete() {
        this.status = COMPLETED;
    }

    /** 문의 상태를 대기로 복구 (관리자 답변 삭제 시 호출) */
    public void reopen() {
        this.status = WAITING;
    }

    /** 답변 참조 제거 - orphanRemoval에 의해 DB에서도 자동 삭제됨 */
    public void clearAnswer() {
        this.answer = null;
    }
}
