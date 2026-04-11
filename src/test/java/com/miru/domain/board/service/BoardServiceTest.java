package com.miru.domain.board.service;

import com.miru.domain.alarm.service.AlarmService;
import com.miru.domain.board.dto.*;
import com.miru.domain.board.entity.Board;
import com.miru.domain.board.entity.BoardType;
import com.miru.domain.board.entity.Comment;
import com.miru.domain.board.repository.BoardLikeRepository;
import com.miru.domain.board.repository.BoardRepository;
import com.miru.domain.board.repository.CommentRepository;
import com.miru.domain.user.entity.Role;
import com.miru.domain.user.entity.User;
import com.miru.domain.user.repository.UserRepository;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.error.BusinessException;
import com.miru.global.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * BoardService 단위 테스트
 * Repository는 Mockito로 mock하여 서비스 비즈니스 로직만 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BoardLikeRepository boardLikeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AlarmService alarmService;

    @InjectMocks
    private BoardService boardService;

    private User user;
    private User adminUser;
    private User otherUser;
    private SessionUser sessionUser;
    private SessionUser adminSessionUser;
    private SessionUser otherSessionUser;
    private Board board;

    @BeforeEach
    void setUp() {
        // 일반 유저
        user = User.builder()
                .email("user@test.com")
                .nickname("일반유저")
                .loginFrom("google")
                .loginFromId("google-user-1")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.activate();

        // 관리자 유저
        adminUser = User.builder()
                .email("admin@test.com")
                .nickname("관리자")
                .loginFrom("google")
                .loginFromId("google-admin-1")
                .role(Role.ADMIN)
                .build();
        ReflectionTestUtils.setField(adminUser, "id", 2L);
        adminUser.activate();

        // 다른 일반 유저
        otherUser = User.builder()
                .email("other@test.com")
                .nickname("다른유저")
                .loginFrom("naver")
                .loginFromId("naver-user-1")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(otherUser, "id", 3L);
        otherUser.activate();

        sessionUser = new SessionUser(user);
        adminSessionUser = new SessionUser(adminUser);
        otherSessionUser = new SessionUser(otherUser);

        // 테스트용 게시글 (일반 유저 작성, GENERAL 타입)
        board = Board.builder()
                .user(user)
                .title("테스트 게시글")
                .content("테스트 내용")
                .type(BoardType.GENERAL)
                .build();
        ReflectionTestUtils.setField(board, "id", 1L);
    }

    // ===================== createBoard =====================

    @Nested
    @DisplayName("createBoard - 게시글 작성")
    class CreateBoardTest {

        private BoardCreateRequestDto dto;

        @BeforeEach
        void setUp() {
            dto = new BoardCreateRequestDto();
            ReflectionTestUtils.setField(dto, "title", "새 게시글");
            ReflectionTestUtils.setField(dto, "content", "내용입니다.");
        }

        @Test
        @DisplayName("일반 유저가 작성하면 type이 GENERAL로 설정된다")
        void createBoard_일반유저_GENERAL() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(boardRepository.save(any(Board.class))).willAnswer(invocation -> {
                Board b = invocation.getArgument(0);
                ReflectionTestUtils.setField(b, "id", 10L);
                return b;
            });
            given(commentRepository.findTopCommentsWithReplies(anyLong())).willReturn(List.of());

            // when
            BoardDetailResponseDto result = boardService.createBoard(sessionUser, dto);

            // then
            assertThat(result.getItems()).hasSize(1);
            // 저장된 게시글의 type 확인을 위해 save 호출 시 인자 캡처
            verify(boardRepository).save(argThat(b -> b.getType() == BoardType.GENERAL));
        }

        @Test
        @DisplayName("관리자가 작성하면 type이 NOTICE로 자동 설정된다")
        void createBoard_관리자_NOTICE() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(adminUser));
            given(boardRepository.save(any(Board.class))).willAnswer(invocation -> {
                Board b = invocation.getArgument(0);
                ReflectionTestUtils.setField(b, "id", 11L);
                return b;
            });
            given(commentRepository.findTopCommentsWithReplies(anyLong())).willReturn(List.of());

            // when
            boardService.createBoard(adminSessionUser, dto);

            // then - 관리자이면 type = NOTICE
            verify(boardRepository).save(argThat(b -> b.getType() == BoardType.NOTICE));
        }

        @Test
        @DisplayName("비로그인 유저: UNAUTHORIZED 예외 발생")
        void createBoard_비로그인() {
            assertThatThrownBy(() -> boardService.createBoard(null, dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.UNAUTHORIZED);
        }
    }

    // ===================== getBoard =====================

    @Nested
    @DisplayName("getBoard - 게시글 상세 조회")
    class GetBoardTest {

        @Test
        @DisplayName("게시글 조회 성공: 조회수가 1 증가한다")
        void getBoard_성공_조회수증가() {
            // given
            int beforeViewCount = board.getViewCount();
            given(boardRepository.findById(1L)).willReturn(Optional.of(board));
            given(boardLikeRepository.existsByUserIdAndBoardId(1L, 1L)).willReturn(false);
            given(commentRepository.findTopCommentsWithReplies(1L)).willReturn(List.of());

            // when
            boardService.getBoard(1L, sessionUser);

            // then
            assertThat(board.getViewCount()).isEqualTo(beforeViewCount + 1);
        }

        @Test
        @DisplayName("존재하지 않는 게시글: BOARD_NOT_FOUND 예외 발생")
        void getBoard_없는게시글() {
            // given
            given(boardRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> boardService.getBoard(999L, sessionUser))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.BOARD_NOT_FOUND);
        }

        @Test
        @DisplayName("비로그인 유저도 게시글 조회 가능: isLiked = false")
        void getBoard_비로그인_조회가능() {
            // given
            given(boardRepository.findById(1L)).willReturn(Optional.of(board));
            given(commentRepository.findTopCommentsWithReplies(1L)).willReturn(List.of());

            // when
            BoardDetailResponseDto result = boardService.getBoard(1L, null);

            // then
            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getItems().get(0).isLiked()).isFalse();
        }
    }

    // ===================== updateBoard =====================

    @Nested
    @DisplayName("updateBoard - 게시글 수정")
    class UpdateBoardTest {

        private BoardUpdateRequestDto dto;

        @BeforeEach
        void setUp() {
            dto = new BoardUpdateRequestDto();
            ReflectionTestUtils.setField(dto, "title", "수정된 제목");
            ReflectionTestUtils.setField(dto, "content", "수정된 내용");
        }

        @Test
        @DisplayName("작성자가 수정하면 제목과 내용이 변경된다")
        void updateBoard_성공() {
            // given
            given(boardRepository.findById(1L)).willReturn(Optional.of(board));
            given(boardLikeRepository.existsByUserIdAndBoardId(1L, 1L)).willReturn(false);
            given(commentRepository.findTopCommentsWithReplies(1L)).willReturn(List.of());

            // when
            boardService.updateBoard(1L, sessionUser, dto);

            // then
            assertThat(board.getTitle()).isEqualTo("수정된 제목");
            assertThat(board.getContent()).isEqualTo("수정된 내용");
        }

        @Test
        @DisplayName("다른 유저가 수정 시도: FORBIDDEN 예외 발생")
        void updateBoard_권한없음() {
            // given - board는 user(id=1)가 작성했는데 otherUser(id=3)가 수정 시도
            given(boardRepository.findById(1L)).willReturn(Optional.of(board));

            // when & then
            assertThatThrownBy(() -> boardService.updateBoard(1L, otherSessionUser, dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.FORBIDDEN);
        }
    }

    // ===================== deleteBoard =====================

    @Nested
    @DisplayName("deleteBoard - 게시글 삭제")
    class DeleteBoardTest {

        @Test
        @DisplayName("작성자가 삭제하면 delete()가 호출된다")
        void deleteBoard_작성자삭제() {
            // given
            given(boardRepository.findById(1L)).willReturn(Optional.of(board));

            // when
            boardService.deleteBoard(1L, sessionUser);

            // then
            verify(boardRepository, times(1)).delete(board);
        }

        @Test
        @DisplayName("관리자가 타인의 게시글 삭제 성공")
        void deleteBoard_관리자삭제() {
            // given
            given(boardRepository.findById(1L)).willReturn(Optional.of(board));

            // when - 관리자(id=2)가 user(id=1)의 게시글 삭제
            boardService.deleteBoard(1L, adminSessionUser);

            // then
            verify(boardRepository, times(1)).delete(board);
        }

        @Test
        @DisplayName("타인(비관리자)이 삭제 시도: FORBIDDEN 예외 발생")
        void deleteBoard_권한없음() {
            // given
            given(boardRepository.findById(1L)).willReturn(Optional.of(board));

            // when & then
            assertThatThrownBy(() -> boardService.deleteBoard(1L, otherSessionUser))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.FORBIDDEN);

            verify(boardRepository, never()).delete(any(Board.class));
        }
    }

    // ===================== createComment =====================

    @Nested
    @DisplayName("createComment - 댓글 작성")
    class CreateCommentTest {

        private CommentCreateRequestDto dto;

        @BeforeEach
        void setUp() {
            dto = new CommentCreateRequestDto();
            ReflectionTestUtils.setField(dto, "content", "좋은 글이네요!");
            ReflectionTestUtils.setField(dto, "parentId", null);
        }

        @Test
        @DisplayName("일반 댓글 작성 성공: commentCount가 1 증가한다")
        void createComment_일반댓글() {
            // given
            int beforeCount = board.getCommentCount();
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(boardRepository.findById(1L)).willReturn(Optional.of(board));
            given(commentRepository.save(any(Comment.class))).willAnswer(i -> {
                Comment c = i.getArgument(0);
                ReflectionTestUtils.setField(c, "id", 100L);
                return c;
            });
            given(boardLikeRepository.existsByUserIdAndBoardId(1L, 1L)).willReturn(false);
            given(commentRepository.findTopCommentsWithReplies(1L)).willReturn(List.of());

            // when
            boardService.createComment(1L, sessionUser, dto);

            // then
            assertThat(board.getCommentCount()).isEqualTo(beforeCount + 1);
        }

        @Test
        @DisplayName("대댓글 작성 성공: parent가 설정된 댓글 저장")
        void createComment_대댓글() {
            // given - 부모 댓글 설정
            Comment parentComment = Comment.builder()
                    .board(board)
                    .user(otherUser)
                    .context("원댓글")
                    .parent(null)
                    .build();
            ReflectionTestUtils.setField(parentComment, "id", 50L);

            ReflectionTestUtils.setField(dto, "parentId", 50L);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(boardRepository.findById(1L)).willReturn(Optional.of(board));
            given(commentRepository.findById(50L)).willReturn(Optional.of(parentComment));
            given(commentRepository.save(any(Comment.class))).willAnswer(i -> {
                Comment c = i.getArgument(0);
                ReflectionTestUtils.setField(c, "id", 101L);
                return c;
            });
            given(boardLikeRepository.existsByUserIdAndBoardId(1L, 1L)).willReturn(false);
            given(commentRepository.findTopCommentsWithReplies(1L)).willReturn(List.of());

            // when
            boardService.createComment(1L, sessionUser, dto);

            // then - 대댓글이므로 save 호출 시 parent가 설정되어 있어야 함
            verify(commentRepository).save(argThat(c -> c.getParent() != null && c.getParent().getId().equals(50L)));
        }

        @Test
        @DisplayName("대댓글에 댓글 달기 시도: NESTED_REPLY_NOT_ALLOWED 예외 발생")
        void createComment_대댓글에댓글_불가() {
            // given - parentComment는 이미 대댓글(parent가 있음)
            Comment grandParent = Comment.builder()
                    .board(board)
                    .user(user)
                    .context("최상위 댓글")
                    .parent(null)
                    .build();
            ReflectionTestUtils.setField(grandParent, "id", 40L);

            Comment parentComment = Comment.builder()
                    .board(board)
                    .user(otherUser)
                    .context("대댓글")
                    .parent(grandParent) // 이미 대댓글
                    .build();
            ReflectionTestUtils.setField(parentComment, "id", 50L);

            ReflectionTestUtils.setField(dto, "parentId", 50L);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(boardRepository.findById(1L)).willReturn(Optional.of(board));
            given(commentRepository.findById(50L)).willReturn(Optional.of(parentComment));

            // when & then
            assertThatThrownBy(() -> boardService.createComment(1L, sessionUser, dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.NESTED_REPLY_NOT_ALLOWED);
        }

        @Test
        @DisplayName("비로그인 유저: UNAUTHORIZED 예외 발생")
        void createComment_비로그인() {
            assertThatThrownBy(() -> boardService.createComment(1L, null, dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.UNAUTHORIZED);
        }
    }

    // ===================== updateComment =====================

    @Nested
    @DisplayName("updateComment - 댓글 수정")
    class UpdateCommentTest {

        private Comment comment;
        private CommentUpdateRequestDto dto;

        @BeforeEach
        void setUp() {
            comment = Comment.builder()
                    .board(board)
                    .user(user)
                    .context("원래 댓글 내용")
                    .parent(null)
                    .build();
            ReflectionTestUtils.setField(comment, "id", 100L);

            dto = new CommentUpdateRequestDto();
            ReflectionTestUtils.setField(dto, "content", "수정된 댓글");
        }

        @Test
        @DisplayName("댓글 수정 성공: 내용이 변경된다")
        void updateComment_성공() {
            // given
            given(commentRepository.findById(100L)).willReturn(Optional.of(comment));
            given(boardLikeRepository.existsByUserIdAndBoardId(1L, 1L)).willReturn(false);
            given(commentRepository.findTopCommentsWithReplies(1L)).willReturn(List.of());

            // when
            boardService.updateComment(100L, sessionUser, dto);

            // then
            assertThat(comment.getContent()).isEqualTo("수정된 댓글");
        }

        @Test
        @DisplayName("삭제된 댓글 수정 시도: DELETED_COMMENT_MODIFY 예외 발생")
        void updateComment_삭제된댓글() {
            // given - 소프트 삭제된 댓글
            comment.delete(); // isDeleted = true, content = "삭제된 메시지입니다."
            given(commentRepository.findById(100L)).willReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> boardService.updateComment(100L, sessionUser, dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.DELETED_COMMENT_MODIFY);
        }

        @Test
        @DisplayName("타인의 댓글 수정 시도: FORBIDDEN 예외 발생")
        void updateComment_권한없음() {
            // given - user의 댓글을 otherUser가 수정 시도
            given(commentRepository.findById(100L)).willReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> boardService.updateComment(100L, otherSessionUser, dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.FORBIDDEN);
        }
    }

    // ===================== deleteComment =====================

    @Nested
    @DisplayName("deleteComment - 댓글 삭제")
    class DeleteCommentTest {

        private Comment comment;

        @BeforeEach
        void setUp() {
            comment = Comment.builder()
                    .board(board)
                    .user(user)
                    .context("삭제할 댓글")
                    .parent(null)
                    .build();
            ReflectionTestUtils.setField(comment, "id", 100L);
        }

        @Test
        @DisplayName("작성자가 삭제: 소프트 삭제되어 isDeleted = true")
        void deleteComment_성공() {
            // given
            given(commentRepository.findById(100L)).willReturn(Optional.of(comment));
            given(boardLikeRepository.existsByUserIdAndBoardId(1L, 1L)).willReturn(false);
            given(commentRepository.findTopCommentsWithReplies(1L)).willReturn(List.of());

            // when
            boardService.deleteComment(100L, sessionUser);

            // then
            assertThat(comment.isDeleted()).isTrue();
            assertThat(comment.getContent()).isEqualTo("삭제된 메시지입니다.");
        }

        @Test
        @DisplayName("관리자가 타인 댓글 삭제: 소프트 삭제 성공")
        void deleteComment_관리자삭제() {
            // given
            given(commentRepository.findById(100L)).willReturn(Optional.of(comment));
            given(boardLikeRepository.existsByUserIdAndBoardId(2L, 1L)).willReturn(false);
            given(commentRepository.findTopCommentsWithReplies(1L)).willReturn(List.of());

            // when
            boardService.deleteComment(100L, adminSessionUser);

            // then
            assertThat(comment.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("타인(비관리자)이 댓글 삭제 시도: FORBIDDEN 예외 발생")
        void deleteComment_권한없음() {
            // given
            given(commentRepository.findById(100L)).willReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> boardService.deleteComment(100L, otherSessionUser))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.FORBIDDEN);
        }
    }
}
