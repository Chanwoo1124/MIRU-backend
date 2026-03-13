package com.miru.domain.agreements.controller;

import com.miru.domain.agreements.service.AgreementsService;
import com.miru.global.auth.annotation.LoginUser;
import com.miru.global.auth.dto.CustomOAuth2User;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agreements")
@RequiredArgsConstructor
public class AgreementsController {

    private final AgreementsService agreementsService;

    /**
     * 약관 동의 API
     * - 이용약관 및 개인정보처리방침 동의 처리
     * - 유저 상태 PENDING → ACTIVE 전환
     * - 세션 내 SecurityContext 갱신 (이후 요청에서 PENDING 필터 통과 가능하도록)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> agree(@LoginUser SessionUser sessionUser) {
        agreementsService.agree(sessionUser);

        // 세션의 SecurityContext에서 status 갱신 (PENDING → ACTIVE)
        refreshSecurityContextStatus();

        return ResponseEntity.ok(ApiResponse.success("약관 동의가 완료되었습니다.", null));
    }

    /** SecurityContextHolder의 CustomOAuth2User status를 ACTIVE로 갱신 */
    private void refreshSecurityContextStatus() {
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

        SecurityContextHolder.getContext().setAuthentication(newToken);
    }
}
