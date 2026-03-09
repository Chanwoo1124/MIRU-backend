package com.miru.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MypageBoardListResponseDto {

    private long totalCount;
    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long id;
        private String title;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDateTime createdAt;
    }
}
