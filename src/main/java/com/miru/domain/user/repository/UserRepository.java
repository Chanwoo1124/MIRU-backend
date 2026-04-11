package com.miru.domain.user.repository;

import com.miru.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * 유저 레포지토리
 *
 * <p>소셜 로그인 식별(loginFrom + loginFromId 조합) 및 이메일/닉네임 중복 확인에 사용되는
 * 커스텀 쿼리 메서드를 제공한다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 소셜 플랫폼 + 소셜 식별값으로 유저 조회
     * CustomOAuth2UserService에서 로그인 시 기존 가입 여부 확인에 사용
     *
     * @param loginFrom   소셜 플랫폼 (google / naver / kakao)
     * @param loginFromId 해당 플랫폼에서 발급한 고유 식별값
     * @return 해당 소셜 계정의 유저 (없으면 null)
     */
    User findByLoginFromAndLoginFromId(String loginFrom, String loginFromId);

    /**
     * 이메일로 유저 조회
     * 동일 이메일로 다른 플랫폼 재가입 시도 감지에 사용
     *
     * @param email 소셜 계정 이메일
     */
    Optional<User> findByEmail(String email);

    /**
     * 닉네임으로 유저 조회
     * 닉네임 변경 시 중복 확인에 사용 (본인 닉네임은 제외 후 확인)
     *
     * @param nickname 확인할 닉네임
     */
    Optional<User> findByNickname(String nickname);

    /**
     * 닉네임 키워드 검색 (관리자 유저 목록 조회용)
     *
     * @param name     검색 키워드 (부분 일치)
     * @param pageable 페이지네이션 정보
     */
    Page<User> findByNicknameContaining(String name, Pageable pageable);
}
