package com.miru.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {

    ACTIVE("정상"),
    BAN("정지"),
    DELETE("탈퇴");

    private final String description;
}