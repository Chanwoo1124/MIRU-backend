package com.miru.domain.inquiry.dto;

import com.miru.domain.inquiry.entity.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryListResponseDto {

    private int totalCount;
    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long id;
        private String title;
        private LocalDateTime createdAt;
        private InquiryStatus status;
    }
}
