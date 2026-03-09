package com.miru.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

    // 공통
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_001", "로그인 세션이 만료되었습니다. 다시 로그인해 주세요."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_002", "해당 작업에 대한 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_003", "서버 오류로 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요."),

    // 인증
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH_001", "이미 가입된 이메일입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다."),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_003", "지원하지 않는 소셜 로그인입니다."),
    EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH_005", "이메일 정보 제공 동의가 필요합니다."),

    // 유저
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "해당 유저를 찾을 수 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "USER_002", "이미 사용중인 닉네임입니다."),

    // 게시판
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_001", "해당 게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_002", "해당 댓글을 찾을 수 없습니다."),
    DELETED_COMMENT_MODIFY(HttpStatus.BAD_REQUEST, "BOARD_003", "삭제된 댓글은 수정할 수 없습니다."),
    NESTED_REPLY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "BOARD_004", "대댓글에는 댓글을 달 수 없습니다."),

    // 자기분석
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "ANALYSIS_001", "해당 질문을 찾을 수 없습니다."),
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "ANALYSIS_002", "해당 답변을 찾을 수 없습니다."),

    // 문의 게시판
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQUIRY_001", "해당 문의를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
