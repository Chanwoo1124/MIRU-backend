package com.miru.global.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miru.global.auth.dto.CustomOAuth2User;
import com.miru.global.common.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PendingUserFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    /** PENDING 유저가 접근 가능한 경로 목록 */
    private static final List<String> PENDING_ALLOWED_PATHS = List.of(
            "/api/agreements",
            "/api/me",
            "/api/logout",
            "/oauth2",
            "/login"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증되지 않은 요청은 통과
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        // OAuth2 유저가 아닌 경우 통과
        if (!(authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User)) {
            filterChain.doFilter(request, response);
            return;
        }

        String status = oAuth2User.getStatus();
        String requestUri = request.getRequestURI();

        // PENDING 유저가 허용되지 않은 경로에 접근하면 차단
        if ("PENDING".equals(status) && isNotAllowedForPending(requestUri)) {
            sendForbiddenResponse(response, "약관 동의가 필요합니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isNotAllowedForPending(String requestUri) {
        return PENDING_ALLOWED_PATHS.stream().noneMatch(requestUri::startsWith);
    }

    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(message));
    }
}
