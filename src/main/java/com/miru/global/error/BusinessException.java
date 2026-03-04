package com.miru.global.error;

import lombok.Getter;

/**
 * 비즈니스 로직 예외 (도메인 전반에서 사용)
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorType errorType;

    public BusinessException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }
}
