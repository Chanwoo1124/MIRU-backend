package com.miru.global.auth.service;

import com.miru.domain.user.entity.Role;
import com.miru.domain.user.entity.User;
import com.miru.domain.user.entity.UserStatus;
import com.miru.domain.user.repository.UserRepository;
import com.miru.global.auth.dto.*;
import com.miru.global.error.ErrorType;
import com.miru.global.error.OAuth2BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 커스텀 OAuth2 유저 서비스
 *
 * <p>소셜 로그인 인증 완료 후 유저 정보를 처리한다.
 * Spring Security의 {@link DefaultOAuth2UserService}를 확장하여 플랫폼별 응답 정규화 및
 * DB 회원가입/로그인 로직을 수행한다.
 *
 * <p>처리 흐름:
 * <ol>
 *   <li>소셜 플랫폼에서 유저 정보 수신 (super.loadUser)</li>
 *   <li>플랫폼별 응답 파싱 (Google/Naver/Kakao → OAuth2Response)</li>
 *   <li>신규 유저: 임시 닉네임(user_xxxxxxxx)으로 DB 저장, 상태 PENDING</li>
 *   <li>기존 유저: 탈퇴 계정이면 reactivate(), 관리자 계정이면 promoteToAdmin()</li>
 *   <li>CustomOAuth2User 반환 → 세션에 저장</li>
 * </ol>
 *
 * <p>관리자 계정: {@code app.admin-google-ids} 환경변수에 등록된 Google 계정 ID만 ADMIN 역할 부여
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Value("${app.admin-google-ids}")
    private java.util.Set<String> adminGoogleIds;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 어느 플랫폼에서 로그인했는지 값을 가져와 변수에 저장
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 플랫폼에 맞춰 데이터 정리
        OAuth2Response oAuth2Response = extractProviderInfo(registrationId, oAuth2User);

        // 로그인 / 회원가입 진행
        User userEntity = validateAndRegister(oAuth2Response);

        return new CustomOAuth2User(
                oAuth2Response,
                userEntity.getRole().getKey(),
                userEntity.getStatus().name(),
                new SessionUser(userEntity)
        );
    }

    // 소셜 플랫폼에 맞춰 데이터 주입하는 메소드
    private OAuth2Response extractProviderInfo(String registrationId, OAuth2User oAuth2User) {
        // 네이버 로그인 시
        if (registrationId.equals("naver")) {
            return new NaverResponse(oAuth2User.getAttributes());
        }
        // 구글 로그인 시
        else if (registrationId.equals("google")) {
            return new GoogleResponse(oAuth2User.getAttributes());
        }
        // 카카오 로그인 시
        else if (registrationId.equals("kakao")) {
            return new KakaoResponse(oAuth2User.getAttributes());
        }

        throw new OAuth2BusinessException(ErrorType.UNSUPPORTED_PROVIDER);
    }

    private User validateAndRegister(OAuth2Response response) {
        // 이메일이 넘어오지 않았을 때 에러처리
        if (response.getEmail() == null || response.getEmail().isEmpty()) {
            throw new OAuth2BusinessException(ErrorType.EMAIL_REQUIRED);
        }

        String provider = response.getProvider();
        String providerId = response.getProviderId();
        String email = response.getEmail();

        // 관리자 구글 계정 여부 확인
        boolean isAdmin = provider.equals("google") && adminGoogleIds.contains(providerId);

        // db에 등록된 유저 있는지 찾기
        User user = userRepository.findByLoginFromAndLoginFromId(provider, providerId);

        // 탈퇴한 유저가 동일 소셜 계정으로 재가입 시 계정 초기화
        if (user != null && user.getStatus() == UserStatus.DELETE) {
            user.reactivate();
            return user;
        }

        // userEntity가 비어있을 때 (등록된 유저가 없을 때)
        if (user == null) {
            // 유저가 동일한 이메일로 회원가입 시도 시
            userRepository.findByEmail(email).ifPresent(existingUser -> {
                throw new OAuth2BusinessException(ErrorType.DUPLICATE_EMAIL);
            });

            String tempNickname = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

            User newUser = User.builder()
                    .loginFrom(provider)
                    .loginFromId(providerId)
                    .nickname(tempNickname)
                    .email(email)
                    .role(isAdmin ? Role.ADMIN : Role.USER)
                    .build();

            userRepository.save(newUser);

            user = newUser;
        } else if (isAdmin && user.getRole() != Role.ADMIN) {
            // 기존 유저인데 관리자 계정이면 ADMIN으로 업그레이드
            user.promoteToAdmin();
        }

        return user;
    }
}