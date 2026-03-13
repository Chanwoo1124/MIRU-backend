package com.miru.domain.agreements.service;

import com.miru.domain.agreements.entity.AgreementType;
import com.miru.domain.agreements.entity.UserAgreement;
import com.miru.domain.agreements.repository.UserAgreementRepository;
import com.miru.domain.user.entity.User;
import com.miru.domain.user.entity.UserStatus;
import com.miru.domain.user.repository.UserRepository;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.error.BusinessException;
import com.miru.global.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgreementsService {

    private final UserRepository userRepository;
    private final UserAgreementRepository userAgreementRepository;

    private static final String TOS_VERSION = "v1.0";

    /**
     * 약관 동의 처리
     * - 이용약관 및 개인정보처리방침 동의 저장
     * - 유저 상태 PENDING → ACTIVE 전환
     */
    @Transactional
    public void agree(SessionUser sessionUser) {
        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorType.USER_NOT_FOUND));

        // PENDING 상태가 아닌 유저는 약관 동의 불가 (ACTIVE 재호출, BAN 우회 방지)
        if (user.getStatus() != UserStatus.PENDING) {
            throw new BusinessException(ErrorType.FORBIDDEN);
        }

        // 약관 동의 저장 (이용약관, 개인정보처리방침)
        userAgreementRepository.save(UserAgreement.builder()
                .user(user)
                .agreementType(AgreementType.TERMS_OF_SERVICE)
                .isAgreed(true)
                .tosVersion(TOS_VERSION)
                .build());

        userAgreementRepository.save(UserAgreement.builder()
                .user(user)
                .agreementType(AgreementType.PRIVACY_POLICY)
                .isAgreed(true)
                .tosVersion(TOS_VERSION)
                .build());

        // 유저 상태 ACTIVE로 전환
        user.activate();
    }
}
