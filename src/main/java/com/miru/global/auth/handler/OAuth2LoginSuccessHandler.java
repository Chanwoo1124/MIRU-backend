package com.miru.global.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.miru.global.auth.dto.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 핸들러
 *
 * <p>소셜 로그인이 성공하면 유저 상태에 따라 프론트엔드 페이지로 리다이렉트한다:
 * <ul>
 *   <li>PENDING (약관 미동의): {@code frontendUrl/terms} 로 리다이렉트</li>
 *   <li>ACTIVE/그 외: {@code frontendUrl/analysis} 로 리다이렉트</li>
 * </ul>
 * {@code app.frontend-url} 환경변수로 대상 URL을 관리하여 환경별 분기가 가능하다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // 약관 미동의(PENDING) 유저는 약관 동의 페이지로 리다이렉트
        String targetUrl = "PENDING".equals(oAuth2User.getStatus())
                ? frontendUrl + "/terms"
                : frontendUrl + "/analysis";

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

