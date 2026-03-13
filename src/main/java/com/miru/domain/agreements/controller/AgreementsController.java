package com.miru.domain.agreements.controller;

import com.miru.domain.agreements.service.AgreementsService;
import com.miru.global.auth.annotation.LoginUser;
import com.miru.global.auth.dto.CustomOAuth2User;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agreements")
@RequiredArgsConstructor
public class AgreementsController {

    private final AgreementsService agreementsService;
    private final HttpSessionSecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    /**
     * 약관 동의 API
     * - 이용약관 및 개인정보처리방침 동의 처리
     * - 유저 상태 PENDING → ACTIVE 전환
     * - 세션 내 SecurityContext 갱신 (이후 요청에서 PENDING 필터 통과 가능하도록)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> agree(
            @LoginUser SessionUser sessionUser,
            HttpServletRequest request,
            HttpServletResponse response) {

        agreementsService.agree(sessionUser);

        // Spring Security 6: SecurityContext를 세션에 명시적으로 저장해야 다음 요청에 반영됨
        refreshSecurityContextStatus(request, response);

        return ResponseEntity.ok(ApiResponse.success("약관 동의가 완료되었습니다.", null));
    }

    /** SecurityContext의 status를 ACTIVE로 갱신하고 세션에 저장 */
    private void refreshSecurityContextStatus(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof OAuth2AuthenticationToken oAuth2Token)) {
            return;
        }

        CustomOAuth2User oldUser = (CustomOAuth2User) oAuth2Token.getPrincipal();
        SessionUser updatedSessionUser = new SessionUser(oldUser.getSessionUser(), "ACTIVE");

        CustomOAuth2User updatedUser = new CustomOAuth2User(
                oldUser.getOAuth2Response(),
                oldUser.getRole(),
                "ACTIVE",
                updatedSessionUser
        );

        OAuth2AuthenticationToken newToken = new OAuth2AuthenticationToken(
                updatedUser,
                updatedUser.getAuthorities(),
                oAuth2Token.getAuthorizedClientRegistrationId()
        );

        // SecurityContext 갱신 후 세션에 명시적 저장 (Spring Security 6 필수)
        SecurityContext newContext = SecurityContextHolder.createEmptyContext();
        newContext.setAuthentication(newToken);
        SecurityContextHolder.setContext(newContext);
        securityContextRepository.saveContext(newContext, request, response);
    }
}
