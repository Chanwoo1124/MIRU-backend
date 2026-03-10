package com.miru.domain.user.controller;

import com.miru.domain.user.dto.AdminUserBoardListResponseDto;
import com.miru.domain.user.dto.AdminUserCommentListResponseDto;
import com.miru.domain.user.dto.AdminUserListResponseDto;
import com.miru.domain.user.service.AdminUserService;
import com.miru.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /** 유저 목록 조회 / 닉네임 검색 */
    @GetMapping
    public ResponseEntity<ApiResponse<AdminUserListResponseDto>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String name) {
        log.info("[관리자 유저 목록 조회] page={}, name={}", page, name);
        AdminUserListResponseDto response = adminUserService.getUsers(page, name);
        return ResponseEntity.ok(ApiResponse.success("유저 목록을 불러왔습니다.", response));
    }

    /** 특정 유저의 작성글 목록 조회 */
    @GetMapping("/{userId}/boards")
    public ResponseEntity<ApiResponse<AdminUserBoardListResponseDto>> getUserBoards(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page) {
        log.info("[관리자 유저 작성글 조회] userId={}, page={}", userId, page);
        AdminUserBoardListResponseDto response = adminUserService.getUserBoards(userId, page);
        return ResponseEntity.ok(ApiResponse.success("유저 작성글 목록을 불러왔습니다.", response));
    }

    /** 특정 유저의 댓글 목록 조회 */
    @GetMapping("/{userId}/comments")
    public ResponseEntity<ApiResponse<AdminUserCommentListResponseDto>> getUserComments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page) {
        log.info("[관리자 유저 댓글 조회] userId={}, page={}", userId, page);
        AdminUserCommentListResponseDto response = adminUserService.getUserComments(userId, page);
        return ResponseEntity.ok(ApiResponse.success("유저 댓글 목록을 불러왔습니다.", response));
    }

    /** 유저 정지 / 정지 해제 */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleUserStatus(
            @PathVariable Long id) {
        log.info("[관리자 유저 상태 변경] userId={}", id);
        String message = adminUserService.toggleUserStatus(id);
        Map<String, Object> data = new HashMap<>();
        data.put("items", Collections.emptyList());
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }
}
