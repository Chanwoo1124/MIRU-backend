package com.miru.domain.user.controller;

import com.miru.global.auth.annotation.LoginUser;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {

    /** 현재 로그인된 유저 정보 조회 */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SessionUser>> getMe(@LoginUser SessionUser sessionUser) {
        log.info("GET /api/me - user: {}", sessionUser.getEmail());
        return ResponseEntity.ok(ApiResponse.success(sessionUser));
    }
}
