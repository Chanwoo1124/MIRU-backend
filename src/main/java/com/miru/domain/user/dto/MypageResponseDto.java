package com.miru.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MypageResponseDto {

    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String nickname;
        private AnalysisStats analysisStats;
        private PostStats postStats;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisStats {
        private long inProgressCount;
        private long completedCount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostStats {
        private long articleCount;
        private long commentCount;
    }
}
