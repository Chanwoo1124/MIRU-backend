package com.miru.domain.analysis.entity;

import com.miru.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String content;

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

    /** 답변 내용 및 상태 수정 */
    public void update(String content, AnswerStatus status) {
        this.content = content;
        this.status = status;
    }
}
