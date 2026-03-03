package com.miru.global.error;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class OAuth2BusinessException extends AuthenticationException {

    private final ErrorType errorType;

    public OAuth2BusinessException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }
}
