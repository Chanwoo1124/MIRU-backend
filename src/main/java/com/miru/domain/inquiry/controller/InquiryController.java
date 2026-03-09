package com.miru.domain.inquiry.controller;

import com.miru.domain.inquiry.dto.InquiryCreateRequestDto;
import com.miru.domain.inquiry.dto.InquiryDetailResponseDto;
import com.miru.domain.inquiry.dto.InquiryListResponseDto;
import com.miru.domain.inquiry.service.InquiryService;
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
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;


    /** 내 문의 목록 전체 조회 */
    @GetMapping
    public ResponseEntity<ApiResponse<InquiryListResponseDto>> getInquiries(
            @LoginUser SessionUser sessionUser) {
        log.info("[문의 목록 조회] userId={}", sessionUser != null ? sessionUser.getId() : "비로그인");
        InquiryListResponseDto response = inquiryService.getInquiries(sessionUser);
        return ResponseEntity.ok(ApiResponse.success("문의 목록을 불러왔습니다.", response));
    }

    /** 문의 상세 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InquiryDetailResponseDto>> getInquiry(
            @PathVariable Long id,
            @LoginUser SessionUser sessionUser) {
        log.info("[문의 상세 조회] inquiryId={}, userId={}", id, sessionUser != null ? sessionUser.getId() : "비로그인");
        InquiryDetailResponseDto response = inquiryService.getInquiry(id, sessionUser);
        return ResponseEntity.ok(ApiResponse.success("문의 상세 내용을 불러왔습니다.", response));
    }

    /** 문의 작성 */
    @PostMapping
    public ResponseEntity<ApiResponse<InquiryDetailResponseDto>> createInquiry(
            @LoginUser SessionUser sessionUser,
            @Valid @RequestBody InquiryCreateRequestDto dto) {
        log.info("[문의 작성] userId={}, title={}", sessionUser != null ? sessionUser.getId() : "비로그인", dto.getTitle());
        InquiryDetailResponseDto response = inquiryService.createInquiry(sessionUser, dto);
        return ResponseEntity.ok(ApiResponse.success("문의가 정상적으로 등록되었습니다.", response));
    }

    /** 문의 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteInquiry(
            @PathVariable Long id,
            @LoginUser SessionUser sessionUser) {
        log.info("[문의 삭제] inquiryId={}, userId={}", id, sessionUser != null ? sessionUser.getId() : "비로그인");
        inquiryService.deleteInquiry(id, sessionUser);
        return ResponseEntity.ok(ApiResponse.success("문의글이 정상적으로 삭제되었습니다.",
                new java.util.HashMap<String, Object>() {{ put("items", Collections.emptyList()); }}));
    }
}
