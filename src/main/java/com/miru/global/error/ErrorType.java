package com.miru.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 예외 유형 정의
 *
 * <p>도메인별로 에러 코드 체계를 분리하여 관리한다.
 * HTTP 상태코드, 내부 에러 코드, 사용자 메시지를 묶어 관리한다.
 * GlobalExceptionHandler에서 BusinessException을 처리할 때 이 정보를 응답에 담는다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorType {

    // ===== 공통 =====
    /** 세션 만료 또는 미로그인 상태 */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_001", "로그인 세션이 만료되었습니다. 다시 로그인해 주세요."),
    /** 권한 부족 (타인의 리소스 접근, PENDING 상태 제한 등) */
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_002", "해당 작업에 대한 권한이 없습니다."),
    /** 예상치 못한 서버 내부 오류 */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_003", "서버 오류로 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요."),

    // ===== 인증 =====
    /** 동일 이메일로 이미 가입된 계정이 존재 */
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH_001", "이미 가입된 이메일입니다."),
    /** JWT 또는 OAuth2 토큰이 유효하지 않음 */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다."),
    /** Google/Naver/Kakao 외 지원하지 않는 플랫폼으로 로그인 시도 */
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_003", "지원하지 않는 소셜 로그인입니다."),
    /** 소셜 플랫폼에서 이메일 정보를 제공하지 않은 경우 (카카오 이메일 미동의 등) */
    EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH_004", "이메일 정보 제공 동의가 필요합니다."),

    // ===== 유저 =====
    /** 해당 ID 또는 조건으로 유저를 찾을 수 없음 */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "해당 유저를 찾을 수 없습니다."),
    /** 이미 다른 유저가 사용 중인 닉네임 */
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "USER_002", "이미 사용중인 닉네임입니다."),

    // ===== 게시판 =====
    /** 해당 ID의 게시글 없음 */
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_001", "해당 게시글을 찾을 수 없습니다."),
    /** 해당 ID의 댓글 없음 */
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_002", "해당 댓글을 찾을 수 없습니다."),
    /** 소프트 삭제된 댓글(isDeleted = true)을 수정 시도 */
    DELETED_COMMENT_MODIFY(HttpStatus.BAD_REQUEST, "BOARD_003", "삭제된 댓글은 수정할 수 없습니다."),
    /** 대댓글에 댓글(2depth)을 달려는 시도 - 1depth만 허용 */
    NESTED_REPLY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "BOARD_004", "대댓글에는 댓글을 달 수 없습니다."),

    // ===== 자기분석 =====
    /** 해당 ID의 질문 없음 */
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "ANALYSIS_001", "해당 질문을 찾을 수 없습니다."),
    /** 해당 질문에 대한 유저의 답변 없음 (삭제 시 확인용) */
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "ANALYSIS_002", "해당 답변을 찾을 수 없습니다."),

    // ===== 문의 게시판 =====
    /** 해당 ID의 문의 없음 */
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQUIRY_001", "해당 문의를 찾을 수 없습니다."),
    /** 이미 답변이 등록된 문의에 답변 재등록 시도 */
    INQUIRY_ALREADY_ANSWERED(HttpStatus.BAD_REQUEST, "INQUIRY_002", "이미 답변이 등록된 문의입니다."),
    /** 답변이 없는 문의에 대해 답변 삭제 시도 */
    INQUIRY_ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "INQUIRY_003", "해당 문의에 등록된 답변이 없습니다.");

    /** HTTP 응답 상태코드 */
    private final HttpStatus status;
    /** 클라이언트가 에러 처리에 사용하는 내부 에러 코드 */
    private final String code;
    /** 사용자에게 노출되는 에러 메시지 */
    private final String message;
}
