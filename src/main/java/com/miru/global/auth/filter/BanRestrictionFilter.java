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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * 정지(BAN) 유저 게시글/댓글 작성 차단 필터
 *
 * <p>모든 요청에서 DB의 최신 유저 상태를 확인하여 BAN 유저의 특정 작업을 차단한다.
 * 세션에 캐시된 상태가 아닌 DB를 직접 조회하는 이유:
 * 관리자가 정지 처리해도 기존 세션의 유저는 여전히 ACTIVE로 인식할 수 있기 때문.
 *
 * <p>차단 대상 작업:
 * <ul>
 *   <li>POST /api/boards - 게시글 작성</li>
 *   <li>POST /api/boards/{id}/comment - 댓글 작성</li>
 * </ul>
 *
 * <p>필터 실행 순서: {@code UsernamePasswordAuthenticationFilter} → {@code PendingUserFilter} → 이 필터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BanRestrictionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

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

        // DELETE 처리는 PendingUserFilter에서 담당 (필터 순서: PendingUserFilter → BanRestrictionFilter)

        // BAN 유저가 게시글/댓글 작성 시도 시 차단
        if ("BAN".equals(status) && isBannedOperation(request)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            objectMapper.writeValue(response.getWriter(), ApiResponse.error("정지된 계정입니다."));
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * BAN 유저에게 차단되는 작업 여부 확인
     * - POST /api/boards : 게시글 작성
     * - POST /api/boards/{id}/comment : 댓글 작성
     */
    private boolean isBannedOperation(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return false;
        }

        String uri = request.getRequestURI();

        // 게시글 작성: POST /api/boards (정확히)
        if (uri.equals("/api/boards")) {
            return true;
        }

        // 댓글 작성: POST /api/boards/{id}/comment
        if (uri.matches("/api/boards/\\d+/comment")) {
            return true;
        }

        return false;
    }
}
