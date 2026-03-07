package com.miru.domain.board.controller;

import com.miru.domain.board.dto.BoardDetailResponseDto;
import com.miru.domain.board.dto.CommentUpdateRequestDto;
import com.miru.domain.board.service.BoardService;
import com.miru.global.auth.annotation.LoginUser;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final BoardService boardService;

    /** 댓글 수정 */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardDetailResponseDto>> updateComment(
            @PathVariable Long id,
            @LoginUser SessionUser sessionUser,
            @Valid @RequestBody CommentUpdateRequestDto dto) {
        log.info("PATCH /api/comments/{} - user: {}", id, sessionUser.getEmail());
        return ResponseEntity.ok(ApiResponse.success("댓글이 수정되었습니다.", boardService.updateComment(id, sessionUser, dto)));
    }

    /** 댓글 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardDetailResponseDto>> deleteComment(
            @PathVariable Long id,
            @LoginUser SessionUser sessionUser) {
        log.info("DELETE /api/comments/{} - user: {}", id, sessionUser.getEmail());
        return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다.", boardService.deleteComment(id, sessionUser)));
    }
}
