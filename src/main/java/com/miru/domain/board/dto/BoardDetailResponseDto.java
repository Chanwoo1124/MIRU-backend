package com.miru.domain.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 상세 응답 DTO
 * 상세 조회 / 작성 / 수정 / 댓글 작성·수정·삭제 / 좋아요 공용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardDetailResponseDto {

    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long id;
        private String title;
        private String content;
        private String writer;
        private int viewCount;
        private int likeCount;
        private int commentCount;
        /** 로그인 유저의 좋아요 여부 */
        private boolean isLiked;
        private LocalDateTime createdAt;
        /** 댓글 목록 */
        private List<CommentItem> comments;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentItem {
        private Long id;
        private String writer;
        private String content;
        private LocalDateTime createdAt;
        /** 대댓글 목록 */
        private List<ReplyItem> replies;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplyItem {
        private Long id;
        private String writer;
        private String content;
        private LocalDateTime createdAt;
    }
}