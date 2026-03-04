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
        // 요청을 보낸 Origin 기반으로 프론트엔드 리다이렉트 URL 동적 결정
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");

        String frontendUrl;
        if (origin != null && !origin.isBlank()) {
            frontendUrl = origin;
        } else if (referer != null && !referer.isBlank()) {
            // Referer에서 path 제거하고 origin만 추출
            java.net.URI uri = java.net.URI.create(referer);
            frontendUrl = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() != -1 ? ":" + uri.getPort() : "");
        } else {
            // fallback
            frontendUrl = "http://localhost:3000";
        }

        String targetUrl = frontendUrl + "/analysis";
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

