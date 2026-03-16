package com.miru.global.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miru.domain.user.entity.User;
import com.miru.domain.user.repository.UserRepository;
import com.miru.global.auth.dto.CustomOAuth2User;
import com.miru.global.common.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingUserFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

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

        // DB에서 최신 상태 조회 (세션 캐시 무시)
        Optional<User> userOpt = userRepository.findById(oAuth2User.getSessionUser().getId());
        if (userOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        String status = userOpt.get().getStatus().name();
        String requestUri = request.getRequestURI();

        // DELETE 유저: 세션 강제 무효화 후 401 반환
        if ("DELETE".equals(status)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            SecurityContextHolder.clearContext();
            sendUnauthorizedResponse(response, "탈퇴한 계정입니다. 다시 로그인해 주세요.");
            return;
        }

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

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(message));
    }
}
