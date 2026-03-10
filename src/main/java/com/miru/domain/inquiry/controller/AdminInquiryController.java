package com.miru.domain.inquiry.controller;

import com.miru.domain.inquiry.dto.AdminInquiryAnswerRequestDto;
import com.miru.domain.inquiry.dto.AdminInquiryDetailResponseDto;
import com.miru.domain.inquiry.dto.AdminInquiryListResponseDto;
import com.miru.domain.inquiry.service.AdminInquiryService;
import com.miru.global.auth.annotation.LoginUser;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final AdminInquiryService adminInquiryService;

    /** 전체 문의 목록 조회 */
    @GetMapping
    public ResponseEntity<ApiResponse<AdminInquiryListResponseDto>> getInquiries() {
        log.info("[관리자 문의 목록 조회]");
        AdminInquiryListResponseDto response = adminInquiryService.getInquiries();
        return ResponseEntity.ok(ApiResponse.success("문의 목록을 불러왔습니다.", response));
    }

    /** 문의 상세 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminInquiryDetailResponseDto>> getInquiry(
            @PathVariable Long id) {
        log.info("[관리자 문의 상세 조회] inquiryId={}", id);
        AdminInquiryDetailResponseDto response = adminInquiryService.getInquiry(id);
        return ResponseEntity.ok(ApiResponse.success("문의 상세 내용을 불러왔습니다.", response));
    }

    /** 관리자 답변 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteAnswer(
            @PathVariable Long id) {
        log.info("[관리자 답변 삭제] inquiryId={}", id);
        adminInquiryService.deleteAnswer(id);
        return ResponseEntity.ok(ApiResponse.success("답변이 삭제되었습니다.",
                new java.util.HashMap<String, Object>() {{ put("items", Collections.emptyList()); }}));
    }

    /** 관리자 답변 등록 */
    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> createAnswer(
            @PathVariable Long id,
            @LoginUser SessionUser sessionUser,
            @Valid @RequestBody AdminInquiryAnswerRequestDto dto) {
        log.info("[관리자 답변 등록] inquiryId={}, adminId={}", id, sessionUser.getId());
        adminInquiryService.createAnswer(id, sessionUser, dto);
        return ResponseEntity.ok(ApiResponse.success("답변이 성공적으로 등록되었습니다.",
                new java.util.HashMap<String, Object>() {{ put("items", Collections.emptyList()); }}));
    }
}
