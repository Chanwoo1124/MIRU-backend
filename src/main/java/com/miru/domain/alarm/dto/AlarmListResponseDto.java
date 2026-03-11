package com.miru.domain.alarm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.miru.domain.alarm.entity.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlarmListResponseDto {

    private long totalCount;
    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long id;
        private AlarmType type;
        private String content;
        private String senderNickname;
        private String targetUrl;
        private boolean isRead;
        @JsonFormat(pattern = "yyyy.MM.dd")
        private LocalDateTime createdAt;
    }
}
