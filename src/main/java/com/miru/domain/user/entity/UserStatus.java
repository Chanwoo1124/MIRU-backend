package com.miru.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {

    PENDING("가입 대기"),
    ACTIVE("정상"),
    BAN("정지"),
    DELETE("탈퇴");

    private final String description;
}