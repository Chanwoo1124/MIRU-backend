package com.miru.global.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miru.global.error.ErrorType;
import com.miru.global.error.OAuth2BusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        ErrorType errorType = ErrorType.USER_NOT_FOUND;

        // 커스텀 예외인지 확인
        if (exception instanceof OAuth2BusinessException) {
            errorType = ((OAuth2BusinessException) exception).getErrorType();
        }

        // HTTP 상태 코드 설정 (미리 선언한 Enum에서 값 꺼내쓰기)
        response.setStatus(errorType.getStatus().value());
        // Json 반환
        response.setContentType("application/json;charset=UTF-8");

        // Json 바디 데이터 구성
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("status", errorType.getStatus().value());
        errorData.put("code", errorType.getCode());
        errorData.put("message", errorType.getMessage());

        // Json으로 파싱 후 출력
        response.getWriter().write(objectMapper.writeValueAsString(errorData));
    }
}