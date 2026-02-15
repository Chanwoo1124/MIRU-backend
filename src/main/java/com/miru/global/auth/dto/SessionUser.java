package com.miru.global.auth.dto;

import com.miru.domain.user.entity.User;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable { // ★ implements Serializable 필수
    private Long id;
    private String nickname;
    private String role;
    private String status;
    private String email;

    public SessionUser(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.role = user.getRole().getKey();
        this.status = user.getStatus().name();
        this.email = user.getEmail();
    }
}