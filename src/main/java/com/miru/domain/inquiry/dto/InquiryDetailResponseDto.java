package com.miru.domain.inquiry.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.miru.domain.inquiry.entity.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryDetailResponseDto {

    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long id;
        private String title;
        private String content;
        private LocalDateTime createdAt;
        private InquiryStatus status;
        @JsonInclude(JsonInclude.Include.ALWAYS)
        private AnswerItem answer;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerItem {
        private String adminName;
        private String content;
        private LocalDateTime createdAt;
    }
}
