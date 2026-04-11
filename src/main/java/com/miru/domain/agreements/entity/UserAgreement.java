package com.miru.domain.agreements.entity;

import com.miru.domain.user.entity.User;
import com.miru.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 유저 약관 동의 이력 엔티티
 *
 * <p>유저가 동의한 약관의 종류 및 버전을 기록한다.
 * 신규 가입 또는 재가입 시 AgreementsService에서 두 가지 타입으로 저장한다:
 * <ul>
 *   <li>TERMS_OF_SERVICE   - 이용약관</li>
 *   <li>PRIVACY_POLICY     - 개인정보처리방침</li>
 * </ul>
 * 약관 버전(tosVersion)은 AgreementsService.TOS_VERSION 상수로 관리한다.
 */
@Entity
@Getter
@Table(name = "user_agreements")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 스펙 준수
public class UserAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 약관에 동의한 유저 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 약관 유형 (TERMS_OF_SERVICE / PRIVACY_POLICY) */
    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_type", nullable = false)
    private AgreementType agreementType;

    /**
     * 동의 여부 (현재 구조상 항상 true만 저장)
     * 향후 선택적 동의 항목이 추가될 경우 false도 저장 가능하도록 boolean으로 설계
     */
    @Column(name = "is_agreed", nullable = false)
    private boolean isAgreed;

    /** 동의 당시 약관 버전 (예: "v1.0") */
    @Column(name = "tos_version", nullable = false, length = 20)
    private String tosVersion;


    @Builder
    public UserAgreement(User user, AgreementType agreementType, boolean isAgreed, String tosVersion) {
        this.user = user;
        this.agreementType = agreementType;
        this.isAgreed = isAgreed;
        this.tosVersion = tosVersion;
    }
}
