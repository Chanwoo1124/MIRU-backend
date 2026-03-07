package com.miru.global.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User, Serializable {

    private final OAuth2Response oAuth2Response;
    private final String role;
    private final String status;

    private final SessionUser sessionUser;

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
