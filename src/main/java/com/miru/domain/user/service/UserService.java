package com.miru.domain.user.service;

import com.miru.domain.user.entity.User;
import com.miru.domain.user.repository.UserRepository;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.error.BusinessException;
import com.miru.global.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 유저 서비스
 *
 * <p>현재 로그인된 유저의 최신 정보를 DB에서 조회하여 반환한다.
 * 세션에 저장된 캐시 정보가 아닌 DB 최신 상태를 반영하기 위해 사용된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /** 현재 로그인된 유저 정보 조회 */
    public SessionUser getMe(SessionUser sessionUser) {
        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorType.USER_NOT_FOUND));
        return new SessionUser(user);
    }
}
