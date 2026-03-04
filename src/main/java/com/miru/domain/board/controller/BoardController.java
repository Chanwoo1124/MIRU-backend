package com.miru.domain.board.controller;

import com.miru.domain.board.dto.*;
import com.miru.domain.board.service.BoardLikeService;
import com.miru.domain.board.service.BoardService;
import com.miru.global.auth.annotation.LoginUser;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final BoardLikeService boardLikeService;

    /** 게시글 전체 목록 조회 */
    @GetMapping
    public ResponseEntity<ApiResponse<BoardListResponseDto>> getBoards(
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(ApiResponse.success(boardService.getBoards(page)));
    }

    /** 게시글 제목 검색 */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<BoardListResponseDto>> searchBoards(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(ApiResponse.success(boardService.searchBoards(keyword, page)));
    }

    /** 게시글 작성 */
    @PostMapping
    public ResponseEntity<ApiResponse<BoardDetailResponseDto>> createBoard(
            @LoginUser SessionUser sessionUser,
            @Valid @RequestBody BoardCreateRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success("게시글이 작성되었습니다.", boardService.createBoard(sessionUser, dto)));
    }

    /** 게시글 상세 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardDetailResponseDto>> getBoard(
            @PathVariable Long id,
            @LoginUser SessionUser sessionUser) {
        return ResponseEntity.ok(ApiResponse.success(boardService.getBoard(id, sessionUser)));
    }

    /** 게시글 수정 */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardDetailResponseDto>> updateBoard(
            @PathVariable Long id,
            @LoginUser SessionUser sessionUser,
            @Valid @RequestBody BoardUpdateRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success("게시글이 수정되었습니다.", boardService.updateBoard(id, sessionUser, dto)));
    }

    /** 게시글 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardDetailResponseDto>> deleteBoard(
            @PathVariable Long id,
            @LoginUser SessionUser sessionUser) {
        return ResponseEntity.ok(ApiResponse.success("게시글이 성공적으로 삭제되었습니다.", boardService.deleteBoard(id, sessionUser)));
    }

    /** 댓글 작성 */
    @PostMapping("/{id}/comment")
    public ResponseEntity<ApiResponse<BoardDetailResponseDto>> createComment(
            @PathVariable Long id,
            @LoginUser SessionUser sessionUser,
            @Valid @RequestBody CommentCreateRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(boardService.createComment(id, sessionUser, dto)));
    }

    /** 좋아요 토글 */
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<BoardDetailResponseDto>> toggleLike(
            @PathVariable Long id,
            @LoginUser SessionUser sessionUser) {
        return ResponseEntity.ok(ApiResponse.success(boardLikeService.toggleLike(id, sessionUser)));
    }
}
