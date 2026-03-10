package com.miru.domain.user.repository;

import com.miru.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByLoginFromAndLoginFromId(String loginFrom, String loginFromId);

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    /** 닉네임 검색 (관리자) */
    Page<User> findByNicknameContaining(String name, Pageable pageable);
}
