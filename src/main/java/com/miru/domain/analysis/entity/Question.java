package com.miru.domain.analysis.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 자기분석 질문 엔티티
 *
 * <p>관리자가 미리 등록해두는 자기분석 질문 목록이다.
 * orderId 기준 오름차순으로 정렬하여 유저에게 제공한다.
 * DataInitializer에서 애플리케이션 시작 시 기본 질문 데이터를 삽입한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 스펙 준수
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 질문 내용 (길이 제한 없음) */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 질문 표시 순서
     * findAllByOrderByOrderIdAsc()로 조회하여 항상 동일한 순서로 제공
     */
    @Column(name = "order_id", nullable = false)
    private int orderId;

    /**
     * 질문 생성자 (DataInitializer 전용)
     *
     * @param content 질문 내용
     * @param orderId 표시 순서 (1부터 시작)
     */
    public Question(String content, int orderId) {
        this.content = content;
        this.orderId = orderId;
    }
}
