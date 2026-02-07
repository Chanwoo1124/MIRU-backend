package com.miru.domain.analysis.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "order_id", nullable = false)
    private int orderId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private QuestionTag tag;


    public Question(String content, int orderId, QuestionTag tag) {
        this.content = content;
        this.orderId = orderId;
        this.tag = tag;
    }
}
