package com.miru.global.auth.dto;

public interface OAuth2Response {

    // 플랫폼 이름
    String getProvider();

    // 플랫폼 별 유저 식별 번호
    String getProviderId();

    // 유저 이메일
    String getEmail();
}
