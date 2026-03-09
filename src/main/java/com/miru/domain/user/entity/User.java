package com.miru.domain.user.entity;

import com.miru.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 15)
    private String nickname;

    @Column(name = "login_from", length = 20)
    private String loginFrom; // 로그인 플랫폼 (Google, Naver, kakao)

    @Column(name = "login_from_id")
    private String loginFromId; // 소셜 식별값

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "delete_at")
    private LocalDate deleteAt;


    @Builder
    public User(String email, String nickname, String loginFrom, String loginFromId, Role role) {
        this.email = email;
        this.nickname = nickname;
        this.loginFrom = loginFrom;
        this.loginFromId = loginFromId;
        this.role = role;
        this.status = UserStatus.ACTIVE;
    }

    /** 닉네임 변경 */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /** 회원 탈퇴 처리 (소프트 삭제) */
    public void withdraw() {
        this.status = UserStatus.DELETE;
        this.deleteAt = LocalDate.now();
    }

    /** 탈퇴 유저 재가입 시 계정 초기화 */
    public void reactivate() {
        this.status = UserStatus.ACTIVE;
        this.deleteAt = null;
    }

}
