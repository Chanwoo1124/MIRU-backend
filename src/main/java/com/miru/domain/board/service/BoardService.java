package com.miru.domain.board.service;

import com.miru.domain.board.dto.*;
import com.miru.domain.board.entity.Board;
import com.miru.domain.alarm.entity.AlarmType;
import com.miru.domain.alarm.service.AlarmService;
import com.miru.domain.board.entity.BoardType;
import com.miru.domain.user.entity.Role;
import com.miru.domain.user.entity.UserStatus;
import com.miru.domain.board.entity.Comment;
import com.miru.domain.board.repository.BoardLikeRepository;
import com.miru.domain.board.repository.BoardRepository;
import com.miru.domain.board.repository.CommentRepository;
import com.miru.domain.user.entity.User;
import com.miru.domain.user.repository.UserRepository;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.error.BusinessException;
import com.miru.global.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final UserRepository userRepository;
    private final AlarmService alarmService;

    /** 게시글 전체 목록 조회 (NOTICE 고정, 10개씩 페이징) */
    public BoardListResponseDto getBoards(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Board> boardPage = boardRepository.findAllOrderByNoticeFirst(pageable);

        List<BoardListResponseDto.Item> items = boardPage.getContent().stream()
                .map(b -> new BoardListResponseDto.Item(
                        b.getId(), b.getType(), b.getTitle(), getWriterName(b.getUser()),
                        b.getCommentCount(), b.getLikeCount(), b.getViewCount(), b.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new BoardListResponseDto((int) boardPage.getTotalElements(), items);
    }

    /** 게시글 제목 검색 (10개씩 페이징) */
    public BoardListResponseDto searchBoards(String keyword, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Board> boardPage = boardRepository.findByTitleContaining(keyword, pageable);

        List<BoardListResponseDto.Item> items = boardPage.getContent().stream()
                .map(b -> new BoardListResponseDto.Item(
                        b.getId(), b.getType(), b.getTitle(), getWriterName(b.getUser()),
                        b.getCommentCount(), b.getLikeCount(), b.getViewCount(), b.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new BoardListResponseDto((int) boardPage.getTotalElements(), items);
    }

    /** 게시글 작성 */
    @Transactional
    public BoardDetailResponseDto createBoard(SessionUser sessionUser, BoardCreateRequestDto dto) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorType.USER_NOT_FOUND));

        // 관리자가 작성하면 자동으로 공지글로 설정 (세션이 아닌 DB role로 확인)
        BoardType type = user.getRole() == Role.ADMIN ? BoardType.NOTICE : BoardType.GENERAL;

        Board board = Board.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .type(type)
                .build();
        boardRepository.save(board);

        return buildDetailResponse(board, false);
    }

    /** 게시글 상세 조회 (조회수 증가) */
    @Transactional
    public BoardDetailResponseDto getBoard(Long boardId, SessionUser sessionUser) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorType.BOARD_NOT_FOUND));
        board.incrementViewCount();

        boolean isLiked = sessionUser != null &&
                boardLikeRepository.existsByUserIdAndBoardId(sessionUser.getId(), boardId);

        return buildDetailResponse(board, isLiked);
    }

    /** 게시글 수정 */
    @Transactional
    public BoardDetailResponseDto updateBoard(Long boardId, SessionUser sessionUser, BoardUpdateRequestDto dto) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorType.BOARD_NOT_FOUND));

        if (!board.getUser().getId().equals(sessionUser.getId())) {
            throw new BusinessException(ErrorType.FORBIDDEN);
        }

        board.update(dto.getTitle(), dto.getContent());

        boolean isLiked = boardLikeRepository.existsByUserIdAndBoardId(sessionUser.getId(), boardId);
        return buildDetailResponse(board, isLiked);
    }

    /** 게시글 삭제 */
    @Transactional
    public BoardDetailResponseDto deleteBoard(Long boardId, SessionUser sessionUser) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorType.BOARD_NOT_FOUND));

        if (!board.getUser().getId().equals(sessionUser.getId())) {
            throw new BusinessException(ErrorType.FORBIDDEN);
        }

        boardRepository.delete(board);
        return new BoardDetailResponseDto(Collections.emptyList());
    }

    /** 댓글 작성 */
    @Transactional
    public BoardDetailResponseDto createComment(Long boardId, SessionUser sessionUser, CommentCreateRequestDto dto) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorType.USER_NOT_FOUND));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorType.BOARD_NOT_FOUND));

        Comment parent = null;
        if (dto.getParentId() != null) {
            parent = commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorType.COMMENT_NOT_FOUND));

            // 대댓글에 대댓글 방지
            if (parent.getParent() != null) {
                throw new BusinessException(ErrorType.NESTED_REPLY_NOT_ALLOWED);
            }
        }

        Comment comment = Comment.builder()
                .board(board)
                .user(user)
                .context(dto.getContent())
                .parent(parent)
                .build();
        commentRepository.save(comment);
        board.incrementCommentCount();

        // 알람 생성
        String targetUrl = "/boards/" + boardId;
        if (parent == null) {
            // 일반 댓글: 게시글 작성자에게 알람
            alarmService.createAlarm(
                    board.getUser(), user, AlarmType.COMMENT,
                    user.getNickname() + "님이 게시글에 댓글을 달았습니다.", targetUrl);
        } else {
            // 대댓글: 원댓글 작성자에게 알람
            alarmService.createAlarm(
                    parent.getUser(), user, AlarmType.COMMENT,
                    user.getNickname() + "님이 댓글에 답글을 달았습니다.", targetUrl);
        }

        boolean isLiked = boardLikeRepository.existsByUserIdAndBoardId(sessionUser.getId(), boardId);
        return buildDetailResponse(board, isLiked);
    }

    /** 댓글 수정 */
    @Transactional
    public BoardDetailResponseDto updateComment(Long commentId, SessionUser sessionUser, CommentUpdateRequestDto dto) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorType.COMMENT_NOT_FOUND));

        if (comment.isDeleted()) {
            throw new BusinessException(ErrorType.DELETED_COMMENT_MODIFY);
        }

        if (!comment.getUser().getId().equals(sessionUser.getId())) {
            throw new BusinessException(ErrorType.FORBIDDEN);
        }

        comment.updateContent(dto.getContent());

        Board board = comment.getBoard();
        boolean isLiked = boardLikeRepository.existsByUserIdAndBoardId(sessionUser.getId(), board.getId());
        return buildDetailResponse(board, isLiked);
    }

    /** 댓글 삭제 (내용을 삭제 문구로 변경) */
    @Transactional
    public BoardDetailResponseDto deleteComment(Long commentId, SessionUser sessionUser) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorType.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(sessionUser.getId())) {
            throw new BusinessException(ErrorType.FORBIDDEN);
        }

        comment.delete();

        Board board = comment.getBoard();
        boolean isLiked = boardLikeRepository.existsByUserIdAndBoardId(sessionUser.getId(), board.getId());
        return buildDetailResponse(board, isLiked);
    }

    /** 탈퇴 유저는 "탈퇴한 사용자"로 익명 처리 */
    private String getWriterName(User user) {
        return user.getStatus() == UserStatus.DELETE ? "탈퇴한 사용자" : user.getNickname();
    }

    /** 상세 응답 DTO 빌드 (공통) */
    private BoardDetailResponseDto buildDetailResponse(Board board, boolean isLiked) {
        // fetch join으로 최상위 댓글 + 대댓글 한 번에 조회 (N+1 방지)
        List<Comment> topComments = commentRepository.findTopCommentsWithReplies(board.getId());

        List<BoardDetailResponseDto.CommentItem> commentItems = topComments.stream()
                .map(c -> {
                    List<BoardDetailResponseDto.ReplyItem> replyItems = c.getReplies().stream()
                            .map(r -> new BoardDetailResponseDto.ReplyItem(
                                    r.getId(), getWriterName(r.getUser()), r.getContent(), r.getCreatedAt()
                            ))
                            .collect(Collectors.toList());
                    return new BoardDetailResponseDto.CommentItem(
                            c.getId(), getWriterName(c.getUser()), c.getContent(), c.getCreatedAt(), replyItems
                    );
                })
                .collect(Collectors.toList());

        BoardDetailResponseDto.Item item = new BoardDetailResponseDto.Item(
                board.getId(), board.getTitle(), board.getContent(), getWriterName(board.getUser()),
                board.getViewCount(), board.getLikeCount(), board.getCommentCount(), isLiked, board.getCreatedAt(), commentItems
        );

        return new BoardDetailResponseDto(List.of(item));
    }
}
