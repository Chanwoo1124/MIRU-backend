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
public class AdminInquiryListResponseDto {

    private long totalCount;
    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long id;
        private InquiryStatus status;
        private String writer;
        private String title;
        @JsonFormat(pattern = "yyyy.MM.dd")
        private LocalDateTime createdAt;
    }
}
