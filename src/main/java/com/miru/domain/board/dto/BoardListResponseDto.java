package com.miru.domain.board.dto;

import com.miru.domain.board.entity.BoardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 목록 조회 응답 DTO (전체 조회 / 검색 공용)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardListResponseDto {

    /** 전체 게시글 수 */
    private int totalCount;

    /** 게시글 목록 */
    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long id;
        /** 게시글 타입 (NOTICE: 공지, GENERAL: 일반) */
        private BoardType type;
        private String title;
        private String writer;
        private int commentCount;
        private int likeCount;
        private int viewCount;
        private LocalDateTime createdAt;
    }
}