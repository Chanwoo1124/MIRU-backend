package com.miru.domain.user.service;

import com.miru.domain.board.entity.Board;
import com.miru.domain.board.entity.Comment;
import com.miru.domain.board.repository.BoardRepository;
import com.miru.domain.board.repository.CommentRepository;
import com.miru.domain.user.dto.AdminUserBoardListResponseDto;
import com.miru.domain.user.dto.AdminUserCommentListResponseDto;
import com.miru.domain.user.dto.AdminUserListResponseDto;
import com.miru.domain.user.entity.User;
import com.miru.domain.user.entity.UserStatus;
import com.miru.domain.user.repository.UserRepository;
import com.miru.global.error.BusinessException;
import com.miru.global.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private static final int USER_PAGE_SIZE = 20;
    private static final int BOARD_PAGE_SIZE = 10;
    private static final int COMMENT_PAGE_SIZE = 10;

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    /** 유저 목록 조회 (닉네임 검색 포함) */
    public AdminUserListResponseDto getUsers(int page, String name) {
        Pageable pageable = PageRequest.of(page, USER_PAGE_SIZE);

        Page<User> userPage;
        if (name != null && !name.isBlank()) {
            userPage = userRepository.findByNicknameContaining(name, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<AdminUserListResponseDto.Item> items = userPage.getContent().stream()
                .map(u -> new AdminUserListResponseDto.Item(
                        u.getId(),
                        u.getNickname(),
                        u.getStatus(),
                        u.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new AdminUserListResponseDto(userPage.getTotalElements(), items);
    }

    /** 특정 유저의 작성글 목록 조회 */
    public AdminUserBoardListResponseDto getUserBoards(Long userId, int page) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorType.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, BOARD_PAGE_SIZE);
        Page<Board> boardPage = boardRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<AdminUserBoardListResponseDto.Item> items = boardPage.getContent().stream()
                .map(b -> new AdminUserBoardListResponseDto.Item(
                        b.getId(),
                        b.getTitle(),
                        b.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new AdminUserBoardListResponseDto(user.getNickname(), boardPage.getTotalElements(), items);
    }

    /** 특정 유저의 댓글 목록 조회 */
    public AdminUserCommentListResponseDto getUserComments(Long userId, int page) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorType.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, COMMENT_PAGE_SIZE);
        Page<Comment> commentPage = commentRepository.findByUserIdWithBoard(userId, pageable);

        List<AdminUserCommentListResponseDto.Item> items = commentPage.getContent().stream()
                .map(c -> new AdminUserCommentListResponseDto.Item(
                        c.getId(),
                        c.getBoard().getId(),
                        c.getBoard().getTitle(),
                        c.getContent(),
                        c.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new AdminUserCommentListResponseDto(user.getNickname(), commentPage.getTotalElements(), items);
    }

    /** 유저 정지 / 정지 해제 토글 */
    @Transactional
    public String toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorType.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.DELETE) {
            throw new BusinessException(ErrorType.USER_NOT_FOUND);
        }

        if (user.getStatus() == UserStatus.BAN) {
            user.unban();
            return "유저가 '" + UserStatus.ACTIVE.getDescription() + "' 상태로 변경되었습니다.";
        } else {
            user.ban();
            return "유저가 '" + UserStatus.BAN.getDescription() + "' 상태로 변경되었습니다.";
        }
    }
}
