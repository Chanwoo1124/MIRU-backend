package com.miru.domain.analysis.dto;

import com.miru.domain.analysis.entity.AnswerStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 질문 목록 조회 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionListResponseDto {

    /** 전체 문항 수 */
    private int totalCount;

    /** 질문 + 답변 항목 목록 */
    private List<Item> items;

    /**
     * 질문 + 사용자 답변 항목
     * 비로그인 또는 미답변 시 answerContext, status는 null
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long id;
        private String content;
        private String answerContext;
        private AnswerStatus status;
    }
}
