package com.miru.domain.agreements.repository;

import com.miru.domain.agreements.entity.UserAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 유저 약관 동의 레포지토리
 *
 * <p>약관 동의 이력 저장에 사용된다.
 * 현재는 기본 CRUD(JpaRepository)만 사용하며,
 * 동의 여부 조회가 필요한 경우 커스텀 메서드를 추가한다.
 */
public interface UserAgreementRepository extends JpaRepository<UserAgreement, Long> {
}
