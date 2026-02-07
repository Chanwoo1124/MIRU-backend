package com.miru.domain.agreements.entity;

import com.miru.domain.user.entity.User;
import com.miru.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_agreements", uniqueConstraints = {
        // 한 유저가 같은 버전의 약관에 중복 동의 방지
        @UniqueConstraint(name = "uk_agreement_user_type_version", columnNames = {"user_id", "agreement_type", "tos_version"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_type", nullable = false)
    private AgreementType agreementType;

    @Column(name = "is_agreed", nullable = false)
    private boolean isAgreed;

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