package com.miru.global.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 모든 사용자를 자기분석 페이지로 리다이렉트
        // nip.io 도메인 사용으로 쿠키 공유 가능
        String targetUrl = "http://192.168.0.44.nip.io:3000/analysis";

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

