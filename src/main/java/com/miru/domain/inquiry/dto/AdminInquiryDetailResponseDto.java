package com.miru.domain.inquiry.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.miru.domain.inquiry.entity.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminInquiryDetailResponseDto {

    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long id;
        private String title;
        private String writer;
        @JsonFormat(pattern = "yyyy.MM.dd")
        private LocalDateTime createdAt;
        private InquiryStatus status;
        private String content;
        private String answerContent;
    }
}
