package com.miru.domain.analysis.entity;

import com.miru.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 자기분석 답변 엔티티
 *
 * <p>유저가 자기분석 질문에 작성한 답변을 저장한다.
 * 한 유저는 하나의 질문에 최대 하나의 답변을 가진다 (saveAnswer 로직에서 upsert 처리).
 * 답변 상태(status)를 통해 마이페이지에서 진행 현황을 집계한다:
 * <ul>
 *   <li>IN_PROGRESS  - 작성 중</li>
 *   <li>COMPLETED    - 완료</li>
 * </ul>
 */
@Entity
@Getter
@Table(name = "Answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 스펙 준수
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 답변 작성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 답변이 속한 자기분석 질문 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /** 답변 내용 (길이 제한 없음, null 허용) */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 답변 진행 상태 (IN_PROGRESS / COMPLETED)
     * 마이페이지 통계 집계에 사용됨
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnswerStatus status;

    @Builder
    public Answer(User user, Question question, String content, AnswerStatus status) {
        this.user = user;
        this.question = question;
        this.content = content;
        this.status = status;
    }

    /**
     * 답변 내용 및 상태 수정
     * saveAnswer에서 기존 답변이 존재할 경우 새로 생성하지 않고 이 메서드로 갱신함
     *
     * @param content 변경할 답변 내용
     * @param status  변경할 진행 상태
     */
    public void update(String content, AnswerStatus status) {
        this.content = content;
        this.status = status;
    }
}
