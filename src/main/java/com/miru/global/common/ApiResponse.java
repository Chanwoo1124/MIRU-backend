package com.miru.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 모든 API 응답을 래핑하는 공통 응답 구조
 *
 * <p>클라이언트는 항상 이 구조로 응답을 받는다:
 * <pre>
 * {
 *   "success": true,
 *   "message": "요청이 성공적으로 처리되었습니다.",
 *   "data": { ... }
 * }
 * </pre>
 *
 * <p>{@code @JsonInclude(NON_NULL)}으로 data가 null인 경우 JSON에서 제외된다.
 *
 * @param <T> 응답 데이터 타입
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 직렬화에서 제외 (data = null 이면 JSON에 미포함)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
    /** 성공 응답 기본 메시지 */
    private static final String DEFAULT_SUCCESS_MESSAGE = "요청이 성공적으로 처리되었습니다.";

    /**
     * 기본 성공 메시지로 성공 응답 생성
     *
     * @param data 응답 데이터
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, DEFAULT_SUCCESS_MESSAGE, data);
    }

    /**
     * 커스텀 메시지로 성공 응답 생성
     *
     * @param message 응답 메시지 (예: "로그아웃 되었습니다.")
     * @param data    응답 데이터 (없으면 null)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * 실패 응답 생성 (data는 항상 null)
     *
     * @param message 에러 메시지 (ErrorType.getMessage() 값 또는 커스텀 메시지)
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
