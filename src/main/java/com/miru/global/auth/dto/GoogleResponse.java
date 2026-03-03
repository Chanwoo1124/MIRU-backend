package com.miru.global.auth.dto;

import com.miru.global.error.ErrorType;
import com.miru.global.error.OAuth2BusinessException;

import java.util.Map;

public class GoogleResponse implements OAuth2Response {

    private final Map<String, Object> attributes;

    public GoogleResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        if (attributes == null || attributes.get("sub") == null) {
            throw new OAuth2BusinessException(ErrorType.UNSUPPORTED_PROVIDER);
        }
        return attributes.get("sub").toString();
    }

    @Override
    public String getEmail() {
        if (attributes == null || attributes.get("email") == null) {
            return null;  // CustomOAuth2UserService에서 처리됨
        }
        return attributes.get("email").toString();
    }
}
