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
        this.role = user.getRole().name();
        this.status = user.getStatus().name();
        this.email = user.getEmail();
    }

    /** 세션 갱신 시 status만 변경하는 복사 생성자 */
    public SessionUser(SessionUser original, String newStatus) {
        this.id = original.id;
        this.nickname = original.nickname;
        this.role = original.role;
        this.status = newStatus;
        this.email = original.email;
    }
}