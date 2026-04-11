package com.miru.global.error;

import lombok.Getter;

/**
 * 비즈니스 로직 예외 (도메인 전반에서 사용)
 *
 * <p>서비스 레이어에서 비즈니스 규칙을 위반했을 때 던지는 예외이다.
 * {@link GlobalExceptionHandler}에서 catch하여 HTTP 상태코드와 함께 {@link com.miru.global.common.ApiResponse} 형식으로 반환한다.
 *
 * <p>사용 예시:
 * <pre>
 *   throw new BusinessException(ErrorType.USER_NOT_FOUND);
 * </pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 예외 유형 (HTTP 상태코드, 에러코드, 메시지를 포함) */
    private final ErrorType errorType;

    /**
     * @param errorType 발생한 예외 유형
     */
    public BusinessException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }
}
