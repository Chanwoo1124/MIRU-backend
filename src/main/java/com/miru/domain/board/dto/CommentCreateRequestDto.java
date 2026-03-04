package com.miru.domain.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 댓글 작성 요청 DTO
 */
@Getter
@NoArgsConstructor
public class CommentCreateRequestDto {

    @NotBlank(message = "댓글 내용을 입력해주세요.")
    @Size(max = 500, message = "댓글은 500자 이하로 입력해주세요.")
    private String content;

    /** 부모 댓글 ID (대댓글일 경우 부모 댓글 ID, 최상위 댓글이면 null) */
    private Long parentId;
}
