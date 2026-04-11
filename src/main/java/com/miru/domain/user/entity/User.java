package com.miru.domain.user.entity;

import com.miru.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 사용자 엔티티
 *
 * <p>소셜 로그인(Google/Naver/Kakao) 전용 계정 정보를 관리한다.
 * 계정 상태(status)에 따라 서비스 이용 범위가 결정된다:
 * <ul>
 *   <li>PENDING  - 신규 가입 후 약관 동의 전 (마이페이지 등 제한적 접근)</li>
 *   <li>ACTIVE   - 정상 이용 가능</li>
 *   <li>BAN      - 관리자에 의해 정지된 상태 (게시글/댓글 작성 불가)</li>
 *   <li>DELETE   - 탈퇴 처리 (소프트 삭제, 90일 후 실제 삭제 예정)</li>
 * </ul>
 */
@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 스펙 준수 - 외부 직접 생성 방지
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소셜 계정 이메일 (유일값, 중복 가입 방지에 활용) */
    @Column(nullable = false, unique = true)
    private String email;

    /** 서비스 내 표시 이름 (최대 15자, 유일값) */
    @Column(nullable = false, unique = true, length = 15)
    private String nickname;

    /** 로그인 플랫폼 식별자 (google / naver / kakao) */
    @Column(name = "login_from", length = 20)
    private String loginFrom;

    /** 소셜 플랫폼에서 부여한 고유 식별값 */
    @Column(name = "login_from_id")
    private String loginFromId;

    /** 권한 (USER / ADMIN) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * 계정 상태 (PENDING / ACTIVE / BAN / DELETE)
     * 신규 가입 시 PENDING 으로 시작하며, 약관 동의 완료 후 ACTIVE 로 전환
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    /**
     * 탈퇴 처리 일자 (소프트 삭제 기준일)
     * 탈퇴 전에는 null
     */
    @Column(name = "delete_at")
    private LocalDate deleteAt;


    /**
     * 신규 사용자 생성 빌더
     * 상태는 항상 PENDING으로 시작 (약관 동의 전)
     */
    @Builder
    public User(String email, String nickname, String loginFrom, String loginFromId, Role role) {
        this.email = email;
        this.nickname = nickname;
        this.loginFrom = loginFrom;
        this.loginFromId = loginFromId;
        this.role = role;
        this.status = UserStatus.PENDING; // 가입 즉시 약관 동의 페이지로 이동 필요
    }

    /**
     * 닉네임 변경
     *
     * @param nickname 변경할 닉네임 (중복 체크는 서비스 레이어에서 수행)
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 회원 탈퇴 처리 (소프트 삭제)
     * 상태를 DELETE로 변경하고 탈퇴일을 기록한다.
     * 실제 DB 삭제는 별도 배치 작업에서 수행 예정
     */
    public void withdraw() {
        this.status = UserStatus.DELETE;
        this.deleteAt = LocalDate.now();
    }

    /**
     * 약관 동의 완료 후 계정 활성화
     * PENDING → ACTIVE 상태 전환
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    /**
     * 탈퇴 유저 재가입 시 계정 초기화
     * 동일 소셜 계정으로 재가입하면 기존 계정을 PENDING 상태로 복구하여 약관 재동의 유도
     */
    public void reactivate() {
        this.status = UserStatus.PENDING;
        this.deleteAt = null;
    }

    /**
     * 관리자 계정으로 승격
     * CustomOAuth2UserService에서 adminGoogleIds 설정값과 일치 시 자동 호출
     */
    public void promoteToAdmin() {
        this.role = Role.ADMIN;
    }

    /**
     * 관리자 - 유저 정지 처리
     * BAN 상태로 전환하며, 게시글/댓글 작성이 BanRestrictionFilter에서 차단됨
     */
    public void ban() {
        this.status = UserStatus.BAN;
    }

    /**
     * 관리자 - 유저 정지 해제
     * ACTIVE 상태로 복구
     */
    public void unban() {
        this.status = UserStatus.ACTIVE;
    }

}