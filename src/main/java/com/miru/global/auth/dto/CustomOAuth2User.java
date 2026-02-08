package com.miru.global.auth.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2Response oAuth2Response;
    private final String role;
    private final String status;

    public CustomOAuth2User(OAuth2Response oAuth2Response, String role, String status) {
        this.oAuth2Response = oAuth2Response;
        this.role = role;
        this.status = status;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    // 계정 권한 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new SimpleGrantedAuthority(role));

        return collection;
    }

    // 계정 고유 식별자 반환
    @Override
    public String getName() {
        return oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
    }

    // 계정 상태 반환
    public String getStatus() {
        return status;
    }
}
