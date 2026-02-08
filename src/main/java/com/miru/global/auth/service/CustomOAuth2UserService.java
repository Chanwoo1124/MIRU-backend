package com.miru.global.auth.service;

import com.miru.domain.user.entity.Role;
import com.miru.domain.user.entity.User;
import com.miru.domain.user.repository.UserRepository;
import com.miru.global.auth.dto.*;
import com.miru.global.error.ErrorType;
import com.miru.global.error.OAuth2BusinessException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

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

        // db에 등록된 유저 있는지 찾기
        User user = userRepository.findByLoginFromAndLoginFromId(provider, providerId);

        // userEntity가 비어있을 때 (등록된 유저가 없을 때)
        if (user == null) {
            // 유저가 동일한 이메일로 회원가입 시도 시
            userRepository.findByEmail(email).ifPresent(existingUser -> {
                throw new OAuth2BusinessException(ErrorType.DUPLICATE_EMAIL);
            });

            String tempNickname = "Guest_" + UUID.randomUUID().toString().substring(0, 8);

            User newUser = User.builder()
                    .loginFrom(provider)
                    .loginFromId(providerId)
                    .nickname(tempNickname)
                    .email(email)
                    .role(Role.GUEST)
                    .build();

            userRepository.save(newUser);
            user = newUser;
        }

        return user;
    }
}