package com.miru.domain.board.service;

import com.miru.domain.board.dto.*;
import com.miru.domain.board.entity.Board;
import com.miru.domain.board.entity.BoardType;
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

    /** 게시글 전체 목록 조회 (NOTICE 고정, 10개씩 페이징) */
    public BoardListResponseDto getBoards(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Board> boardPage = boardRepository.findAllOrderByNoticeFirst(pageable);

        List<BoardListResponseDto.Item> items = boardPage.getContent().stream()
                .map(b -> new BoardListResponseDto.Item(
                        b.getId(), b.getType(), b.getTitle(), b.getUser().getNickname(),
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
                        b.getId(), b.getType(), b.getTitle(), b.getUser().getNickname(),
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

        Board board = Board.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .type(BoardType.GENERAL)
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

    /** 상세 응답 DTO 빌드 (공통) */
    private BoardDetailResponseDto buildDetailResponse(Board board, boolean isLiked) {
        List<Comment> topComments = commentRepository
                .findByBoardIdAndParentIsNullOrderByCreatedAtAsc(board.getId());

        List<BoardDetailResponseDto.CommentItem> commentItems = topComments.stream()
                .map(c -> {
                    List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(c.getId());
                    List<BoardDetailResponseDto.ReplyItem> replyItems = replies.stream()
                            .map(r -> new BoardDetailResponseDto.ReplyItem(
                                    r.getId(), r.getUser().getNickname(), r.getContent(), r.getCreatedAt()
                            ))
                            .collect(Collectors.toList());
                    return new BoardDetailResponseDto.CommentItem(
                            c.getId(), c.getUser().getNickname(), c.getContent(), c.getCreatedAt(), replyItems
                    );
                })
                .collect(Collectors.toList());

        BoardDetailResponseDto.Item item = new BoardDetailResponseDto.Item(
                board.getId(), board.getTitle(), board.getContent(), board.getUser().getNickname(),
                board.getViewCount(), board.getLikeCount(), isLiked, board.getCreatedAt(), commentItems
        );

        return new BoardDetailResponseDto(List.of(item));
    }
}
