package com.miru.domain.inquiry.entity;

import com.miru.domain.user.entity.User;
import com.miru.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_board_id", nullable = false)
    private InquiryBoard inquiryBoard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id",nullable = false)
    private User admin;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder
    public InquiryAnswer(InquiryBoard inquiryBoard, User admin, String content) {
        this.inquiryBoard = inquiryBoard;
        this.admin = admin;
        this.content = content;
    }
}
