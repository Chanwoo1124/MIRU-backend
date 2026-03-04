package com.miru.domain.analysis.dto;

import com.miru.domain.analysis.entity.AnswerStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 답변 저장 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerSaveResponseDto {

    /** 저장된 답변 항목 목록 */
    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        /** 질문 ID */
        private Long id;
        /** 질문 내용 */
        private String content;
        /** 답변 내용 */
        private String answerContext;
        /** 답변 상태 */
        private AnswerStatus status;
    }
}