package com.miru.domain.user.controller;

import com.miru.domain.user.entity.User;
import com.miru.domain.user.repository.UserRepository;
import com.miru.global.auth.annotation.LoginUser;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.common.ApiResponse;
import com.miru.global.error.BusinessException;
import com.miru.global.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /** 현재 로그인된 유저 정보 조회 (DB에서 최신 상태 반환) */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SessionUser>> getMe(@LoginUser SessionUser sessionUser) {
        log.info("GET /api/me - user: {}", sessionUser.getEmail());
        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorType.USER_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success(new SessionUser(user)));
    }
}
