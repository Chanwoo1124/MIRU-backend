package com.miru.global.auth.dto;

import com.miru.global.error.ErrorType;
import com.miru.global.error.OAuth2BusinessException;

import java.io.Serializable;
import java.util.Map;

public class NaverResponse implements OAuth2Response, Serializable {

    private final Map<String, Object> attributes;

    public NaverResponse(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            throw new OAuth2BusinessException(ErrorType.UNSUPPORTED_PROVIDER);
        }
        this.attributes = response;
    }

    @Override
    public String getProvider() {
        return "naver";
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
        if (attributes == null || attributes.get("email") == null) {
            return null;  // CustomOAuth2UserService에서 처리됨
        }
        return attributes.get("email").toString();
    }
}
