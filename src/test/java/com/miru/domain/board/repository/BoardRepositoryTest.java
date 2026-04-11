package com.miru.domain.board.repository;

import com.miru.domain.board.entity.Board;
import com.miru.domain.board.entity.BoardType;
import com.miru.domain.user.entity.Role;
import com.miru.domain.user.entity.User;
import com.miru.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BoardRepository 통합 테스트
 * H2 인메모리 DB를 사용하여 커스텀 JPQL 쿼리의 정확성을 검증한다.
 */
@DataJpaTest
@ActiveProfiles("test")
class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 저장
        user = userRepository.save(User.builder()
                .email("user@test.com")
                .nickname("일반유저")
                .loginFrom("google")
                .loginFromId("google-1")
                .role(Role.USER)
                .build());
        user.activate();

        adminUser = userRepository.save(User.builder()
                .email("admin@test.com")
                .nickname("관리자")
                .loginFrom("google")
                .loginFromId("google-2")
                .role(Role.ADMIN)
                .build());
        adminUser.activate();

        em.flush();
    }

    /**
     * 게시글 저장 헬퍼 메서드
     */
    private Board saveBoard(User writer, String title, BoardType type) {
        Board board = Board.builder()
                .user(writer)
                .title(title)
                .content("내용")
                .type(type)
                .build();
        Board saved = boardRepository.save(board);
        em.flush();
        em.clear(); // 영속성 컨텍스트 비워서 실제 쿼리 실행 확인
        return saved;
    }

    // ===================== findAllOrderByNoticeFirst =====================

    @Test
    @DisplayName("공지글(NOTICE)이 일반글(GENERAL)보다 먼저 조회된다")
    void findAllOrderByNoticeFirst_공지글_최상단() {
        // given - 일반글을 먼저 저장하고 이후 공지글 저장
        saveBoard(user, "일반글1", BoardType.GENERAL);
        saveBoard(user, "일반글2", BoardType.GENERAL);
        saveBoard(adminUser, "공지사항", BoardType.NOTICE);

        // when
        Page<Board> result = boardRepository.findAllOrderByNoticeFirst(PageRequest.of(0, 10));

        // then - 첫 번째 결과가 NOTICE 타입이어야 함
        List<Board> boards = result.getContent();
        assertThat(boards).isNotEmpty();
        assertThat(boards.get(0).getType()).isEqualTo(BoardType.NOTICE);
        assertThat(boards.get(0).getTitle()).isEqualTo("공지사항");
    }

    @Test
    @DisplayName("공지글이 여러 개면 모두 GENERAL보다 앞에 위치한다")
    void findAllOrderByNoticeFirst_복수공지() {
        // given
        saveBoard(user, "일반글", BoardType.GENERAL);
        saveBoard(adminUser, "공지1", BoardType.NOTICE);
        saveBoard(adminUser, "공지2", BoardType.NOTICE);

        // when
        Page<Board> result = boardRepository.findAllOrderByNoticeFirst(PageRequest.of(0, 10));
        List<Board> boards = result.getContent();

        // then - 첫 두 개가 모두 NOTICE
        assertThat(boards.get(0).getType()).isEqualTo(BoardType.NOTICE);
        assertThat(boards.get(1).getType()).isEqualTo(BoardType.NOTICE);
        assertThat(boards.get(2).getType()).isEqualTo(BoardType.GENERAL);
    }

    @Test
    @DisplayName("페이징이 정상 동작한다: 2번째 페이지 조회")
    void findAllOrderByNoticeFirst_페이징() {
        // given - 게시글 12개 저장
        for (int i = 1; i <= 12; i++) {
            saveBoard(user, "게시글" + i, BoardType.GENERAL);
        }

        // when - 첫 번째 페이지(0번) 10개
        Page<Board> firstPage = boardRepository.findAllOrderByNoticeFirst(PageRequest.of(0, 10));
        // 두 번째 페이지(1번) 2개
        Page<Board> secondPage = boardRepository.findAllOrderByNoticeFirst(PageRequest.of(1, 10));

        // then
        assertThat(firstPage.getContent()).hasSize(10);
        assertThat(secondPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(12);
    }

    // ===================== findByTitleContaining =====================

    @Test
    @DisplayName("키워드가 포함된 게시글만 반환된다")
    void findByTitleContaining_키워드검색() {
        // given
        saveBoard(user, "Spring Boot 강좌", BoardType.GENERAL);
        saveBoard(user, "Java 기초", BoardType.GENERAL);
        saveBoard(user, "Spring Security 설정", BoardType.GENERAL);

        // when - "Spring" 키워드로 검색
        Page<Board> result = boardRepository.findByTitleContaining("Spring", PageRequest.of(0, 10));

        // then - "Spring Boot 강좌"와 "Spring Security 설정" 2개만 반환
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(Board::getTitle)
                .containsExactlyInAnyOrder("Spring Boot 강좌", "Spring Security 설정");
    }

    @Test
    @DisplayName("검색 결과에서도 공지글이 최상단에 위치한다")
    void findByTitleContaining_공지우선정렬() {
        // given
        saveBoard(user, "Spring 일반글", BoardType.GENERAL);
        saveBoard(adminUser, "Spring 공지글", BoardType.NOTICE);

        // when
        Page<Board> result = boardRepository.findByTitleContaining("Spring", PageRequest.of(0, 10));

        // then
        assertThat(result.getContent().get(0).getType()).isEqualTo(BoardType.NOTICE);
    }

    @Test
    @DisplayName("키워드가 없으면 빈 결과를 반환한다")
    void findByTitleContaining_없는키워드() {
        // given
        saveBoard(user, "일반 게시글", BoardType.GENERAL);

        // when
        Page<Board> result = boardRepository.findByTitleContaining("존재하지않는키워드", PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isZero();
    }
}
