package com.miru.domain.alarm.controller;

import com.miru.domain.alarm.dto.AlarmHasUnreadResponseDto;
import com.miru.domain.alarm.dto.AlarmListResponseDto;
import com.miru.domain.alarm.service.AlarmService;
import com.miru.global.auth.annotation.LoginUser;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    /** 알람 목록 조회 (최신순, 10개씩) */
    @GetMapping
    public ResponseEntity<ApiResponse<AlarmListResponseDto>> getAlarms(
            @LoginUser SessionUser sessionUser,
            @RequestParam(defaultValue = "0") int page) {
        log.info("[알람 목록 조회] userId={}, page={}", sessionUser != null ? sessionUser.getId() : "비로그인", page);
        AlarmListResponseDto response = alarmService.getAlarms(sessionUser, page);
        return ResponseEntity.ok(ApiResponse.success("알람 목록을 불러왔습니다.", response));
    }

    /** 읽지 않은 알람 존재 여부 (빨간 동그라미용) */
    @GetMapping("/has-unread")
    public ResponseEntity<ApiResponse<AlarmHasUnreadResponseDto>> hasUnread(
            @LoginUser SessionUser sessionUser) {
        log.info("[읽지 않은 알람 확인] userId={}", sessionUser != null ? sessionUser.getId() : "비로그인");
        AlarmHasUnreadResponseDto response = alarmService.hasUnread(sessionUser);
        return ResponseEntity.ok(ApiResponse.success("읽지 않은 알람 여부를 확인했습니다.", response));
    }

    /** 단건 읽음 처리 (x 버튼 클릭 시) */
    @PatchMapping("/{alarmId}/read")
    public ResponseEntity<ApiResponse<Object>> readOne(
            @LoginUser SessionUser sessionUser,
            @PathVariable Long alarmId) {
        log.info("[단건 읽음 처리] userId={}, alarmId={}", sessionUser != null ? sessionUser.getId() : "비로그인", alarmId);
        alarmService.readOne(sessionUser, alarmId);
        return ResponseEntity.ok(ApiResponse.success("알람을 읽음 처리했습니다.", null));
    }

    /** 전체 읽음 처리 (종 클릭 시) */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Object>> readAll(
            @LoginUser SessionUser sessionUser) {
        log.info("[전체 읽음 처리] userId={}", sessionUser != null ? sessionUser.getId() : "비로그인");
        alarmService.readAll(sessionUser);
        return ResponseEntity.ok(ApiResponse.success("모든 알람을 읽음 처리했습니다.", null));
    }
}
