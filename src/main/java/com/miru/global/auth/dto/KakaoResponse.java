package com.miru.global.auth.dto;

import com.miru.global.error.ErrorType;
import com.miru.global.error.OAuth2BusinessException;

import java.io.Serializable;
import java.util.Map;

public class KakaoResponse implements OAuth2Response, Serializable {

    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;

    public KakaoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            throw new OAuth2BusinessException(ErrorType.UNSUPPORTED_PROVIDER);
        }
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        if (attributes == null || attributes.get("id") == null) {
            throw new OAuth2BusinessException(ErrorType.UNSUPPORTED_PROVIDER);
        }
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        // 카카오는 사용자가 이메일 제공을 거부할 수 있으므로 null 체크 필수
        if (kakaoAccount == null || kakaoAccount.get("email") == null) {
            return null;  // CustomOAuth2UserService에서 처리됨
        }
        return kakaoAccount.get("email").toString();
    }
}
