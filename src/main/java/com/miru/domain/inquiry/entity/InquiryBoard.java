package com.miru.domain.inquiry.entity;

import com.miru.domain.user.entity.User;
import com.miru.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.miru.domain.inquiry.entity.InquiryStatus.*;

/**
 * 문의 게시글 엔티티
 *
 * <p>유저가 작성한 고객 문의를 관리한다.
 * 문의 상태(status)는 관리자의 답변 등록 여부에 따라 자동으로 변경된다:
 * <ul>
 *   <li>WAITING   - 답변 대기 중 (기본값)</li>
 *   <li>COMPLETED - 관리자 답변 완료</li>
 * </ul>
 * 답변(InquiryAnswer)은 1:1 관계이며, 삭제 시 cascade + orphanRemoval로 함께 삭제된다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 스펙 준수
public class InquiryBoard extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 문의 작성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 문의 제목 (최대 50자) */
    @Column(nullable = false, length = 50)
    private String title;

    /** 문의 내용 (길이 제한 없음) */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 문의 처리 상태 (WAITING / COMPLETED) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status;

    /**
     * 관리자 답변 (1:1)
     * 문의 삭제 시 cascade + orphanRemoval로 답변 레코드도 함께 삭제됨
     * clearAnswer() 호출 시에도 orphanRemoval로 자동 삭제됨
     */
    @OneToOne(mappedBy = "inquiryBoard", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private InquiryAnswer answer;

    /**
     * 문의 생성 빌더
     * 상태는 항상 WAITING으로 시작
     */
    @Builder
    public InquiryBoard(User user, String title, String content) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.status = WAITING; // 신규 문의는 항상 답변 대기 상태
    }

    /**
     * 문의 상태를 COMPLETED로 변경
     * 관리자가 답변을 등록할 때 AdminInquiryService에서 호출
     */
    public void complete() {
        this.status = COMPLETED;
    }

    /**
     * 문의 상태를 WAITING으로 복구
     * 관리자가 답변을 삭제할 때 AdminInquiryService에서 호출
     */
    public void reopen() {
        this.status = WAITING;
    }

    /**
     * 답변 참조를 null로 설정 (답변 삭제용)
     * orphanRemoval = true 이므로 이 메서드 호출 후 flush 시 DB에서 실제 삭제됨
     */
    public void clearAnswer() {
        this.answer = null;
    }
}
