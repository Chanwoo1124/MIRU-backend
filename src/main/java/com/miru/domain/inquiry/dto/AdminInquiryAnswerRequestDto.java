package com.miru.domain.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminInquiryAnswerRequestDto {

    @NotBlank(message = "답변 내용을 입력해 주세요.")
    private String content;
}
