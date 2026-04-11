package com.miru.domain.inquiry.entity;

import com.miru.domain.user.entity.User;
import com.miru.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문의 답변 엔티티
 *
 * <p>관리자가 유저의 문의에 등록하는 답변이다.
 * InquiryBoard와 1:1 관계이며, InquiryBoard의 orphanRemoval 설정으로
 * 문의 삭제 또는 clearAnswer() 호출 시 함께 삭제된다.
 * 답변 등록 후 문의 작성자에게 알람이 발송된다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 스펙 준수
public class InquiryAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 답변이 속한 문의 게시글 (1:1 관계의 외래키 보유 측) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_board_id", nullable = false)
    private InquiryBoard inquiryBoard;

    /** 답변을 작성한 관리자 (User 엔티티의 Role = ADMIN) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    /** 답변 내용 (길이 제한 없음) */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder
    public InquiryAnswer(InquiryBoard inquiryBoard, User admin, String content) {
        this.inquiryBoard = inquiryBoard;
        this.admin = admin;
        this.content = content;
    }
}
