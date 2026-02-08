package com.miru.global.auth.dto;

import com.miru.domain.user.entity.User;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable { // ★ implements Serializable 필수
    private String nickname;
    private String role;
    private String status;

    public SessionUser(User user) {
        this.nickname = user.getNickname();
        this.role = user.getRole().getKey();
        this.status = user.getStatus().name();
    }
}