package com.miru.domain.analysis.dto;

import com.miru.domain.analysis.entity.AnswerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 답변 저장 요청 DTO (임시저장 및 작성완료)
 */
@Getter
@NoArgsConstructor
public class AnswerSaveRequestDto {

    @NotBlank(message = "답변 내용을 입력해주세요.")
    private String answerContext;

    @NotNull(message = "저장 상태를 선택해주세요.")
    private AnswerStatus status;
}
