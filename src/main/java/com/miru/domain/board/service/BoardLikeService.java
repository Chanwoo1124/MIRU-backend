package com.miru.domain.board.service;

import com.miru.domain.board.dto.BoardDetailResponseDto;
import com.miru.domain.board.entity.Board;
import com.miru.domain.board.entity.BoardLike;
import com.miru.domain.board.repository.BoardLikeRepository;
import com.miru.domain.board.repository.BoardRepository;
import com.miru.domain.board.repository.CommentRepository;
import com.miru.domain.user.entity.User;
import com.miru.domain.user.repository.UserRepository;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.error.BusinessException;
import com.miru.global.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardLikeService {

    private final BoardRepository boardRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    /** 좋아요 토글 (있으면 취소, 없으면 추가) */
    @Transactional
    public BoardDetailResponseDto toggleLike(Long boardId, SessionUser sessionUser) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorType.BOARD_NOT_FOUND));
        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorType.USER_NOT_FOUND));

        boolean isLiked;
        if (boardLikeRepository.existsByUserIdAndBoardId(sessionUser.getId(), boardId)) {
            boardLikeRepository.deleteByUserIdAndBoardId(sessionUser.getId(), boardId);
            board.decrementLikeCount();
            isLiked = false;
        } else {
            boardLikeRepository.save(BoardLike.builder().user(user).board(board).build());
            board.incrementLikeCount();
            isLiked = true;
        }

        List<BoardDetailResponseDto.CommentItem> commentItems = commentRepository
                .findByBoardIdAndParentIsNullOrderByCreatedAtAsc(board.getId()).stream()
                .map(c -> {
                    List<BoardDetailResponseDto.ReplyItem> replyItems = commentRepository
                            .findByParentIdOrderByCreatedAtAsc(c.getId()).stream()
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
