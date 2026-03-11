package com.miru.domain.analysis.controller;

import com.miru.domain.analysis.dto.AnswerSaveRequestDto;
import com.miru.domain.analysis.dto.AnswerSaveResponseDto;
import com.miru.domain.analysis.dto.QuestionListResponseDto;
import com.miru.domain.analysis.service.AnalysisService;
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
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    /** 자기분석 질문 목록 조회 (로그인 시 답변 포함) */
    @GetMapping
    public ResponseEntity<ApiResponse<QuestionListResponseDto>> getQuestions(
            @LoginUser SessionUser sessionUser) {
        log.info("GET /api/analysis - user: {}", sessionUser != null ? sessionUser != null ? sessionUser.getEmail() : "비로그인" : "비로그인");
        return ResponseEntity.ok(ApiResponse.success(analysisService.getQuestions(sessionUser)));
    }

    /** 답변 저장 및 수정 (임시저장 or 작성완료) */
    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<AnswerSaveResponseDto>> saveAnswer(
            @PathVariable Long id,
            @LoginUser SessionUser sessionUser,
            @Valid @RequestBody AnswerSaveRequestDto dto) {
        log.info("POST /api/analysis/{} - user: {}", id, sessionUser != null ? sessionUser.getEmail() : "비로그인");
        return ResponseEntity.ok(ApiResponse.success("답변이 저장되었습니다.", analysisService.saveAnswer(id, sessionUser, dto)));
    }

    /** 답변 초기화 */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<AnswerSaveResponseDto>> deleteAnswer(
            @PathVariable Long id,
            @LoginUser SessionUser sessionUser) {
        log.info("DELETE /api/analysis/{} - user: {}", id, sessionUser != null ? sessionUser.getEmail() : "비로그인");
        return ResponseEntity.ok(ApiResponse.success("답변이 삭제되었습니다.", analysisService.deleteAnswer(id, sessionUser)));
    }
}
