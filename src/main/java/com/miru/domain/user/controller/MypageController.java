package com.miru.domain.user.controller;

import com.miru.domain.user.dto.*;
import com.miru.domain.user.service.MypageService;
import com.miru.global.auth.annotation.LoginUser;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.common.ApiResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    /** 마이페이지 기본 정보 조회 */
    @GetMapping
    public ResponseEntity<ApiResponse<MypageResponseDto>> getMypage(
            @LoginUser SessionUser sessionUser) {
        log.info("[마이페이지 조회] userId={}", sessionUser != null ? sessionUser.getId() : "비로그인");
        MypageResponseDto response = mypageService.getMypage(sessionUser);
        return ResponseEntity.ok(ApiResponse.success("마이페이지 정보를 불러왔습니다.", response));
    }

    /** 내가 쓴 게시글 목록 조회 */
    @GetMapping("/boards")
    public ResponseEntity<ApiResponse<MypageBoardListResponseDto>> getMyBoards(
            @LoginUser SessionUser sessionUser,
            @RequestParam(defaultValue = "0") int page) {
        log.info("[내 게시글 목록 조회] userId={}, page={}", sessionUser != null ? sessionUser.getId() : "비로그인", page);
        MypageBoardListResponseDto response = mypageService.getMyBoards(sessionUser, page);
        return ResponseEntity.ok(ApiResponse.success("내 게시글 목록을 불러왔습니다.", response));
    }

    /** 내가 쓴 댓글 목록 조회 */
    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<MypageCommentListResponseDto>> getMyComments(
            @LoginUser SessionUser sessionUser,
            @RequestParam(defaultValue = "0") int page) {
        log.info("[내 댓글 목록 조회] userId={}, page={}", sessionUser != null ? sessionUser.getId() : "비로그인", page);
        MypageCommentListResponseDto response = mypageService.getMyComments(sessionUser, page);
        return ResponseEntity.ok(ApiResponse.success("내 댓글 목록을 불러왔습니다.", response));
    }

    /** 닉네임 변경 */
    @PatchMapping("/nickname")
    public ResponseEntity<ApiResponse<Object>> updateNickname(
            @LoginUser SessionUser sessionUser,
            @Valid @RequestBody NicknameUpdateRequestDto dto) {
        log.info("[닉네임 변경] userId={}, nickname={}", sessionUser != null ? sessionUser.getId() : "비로그인", dto.getNickname());
        String nickname = mypageService.updateNickname(sessionUser, dto);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("nickname", nickname);
        data.put("items", Collections.emptyList());
        return ResponseEntity.ok(ApiResponse.success("닉네임이 성공적으로 변경되었습니다.", data));
    }

    /** 회원 탈퇴 */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Object>> withdraw(
            @LoginUser SessionUser sessionUser,
            HttpSession session) {
        log.info("[회원 탈퇴] userId={}", sessionUser != null ? sessionUser.getId() : "비로그인");
        mypageService.withdraw(sessionUser, session);
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다. 그동안 이용해주셔서 감사합니다.",
                new java.util.HashMap<String, Object>() {{ put("items", Collections.emptyList()); }}));
    }
}
