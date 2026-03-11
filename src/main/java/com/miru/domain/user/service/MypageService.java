package com.miru.domain.user.service;

import com.miru.domain.analysis.entity.AnswerStatus;
import com.miru.domain.analysis.repository.AnswerRepository;
import com.miru.domain.analysis.repository.QuestionRepository;
import com.miru.domain.board.entity.Board;
import com.miru.domain.board.entity.Comment;
import com.miru.domain.board.repository.BoardRepository;
import com.miru.domain.board.repository.CommentRepository;
import com.miru.domain.user.dto.*;
import com.miru.domain.user.entity.User;
import com.miru.domain.user.repository.UserRepository;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.error.BusinessException;
import com.miru.global.error.ErrorType;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageService {

    private static final int PAGE_SIZE = 10;

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    /** 마이페이지 기본 정보 조회 */
    public MypageResponseDto getMypage(SessionUser sessionUser) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorType.USER_NOT_FOUND));

        long inProgressCount = answerRepository.countByUserIdAndStatus(user.getId(), AnswerStatus.IN_PROGRESS);
        long completedCount = answerRepository.countByUserIdAndStatus(user.getId(), AnswerStatus.COMPLETED);
        long totalQuestions = questionRepository.count();
        long notStartedCount = totalQuestions - inProgressCount - completedCount;
        long articleCount = boardRepository.countByUserId(user.getId());
        long commentCount = commentRepository.countByUserIdAndIsDeletedFalse(user.getId());

        MypageResponseDto.AnalysisStats analysisStats = new MypageResponseDto.AnalysisStats(notStartedCount, inProgressCount, completedCount);
        MypageResponseDto.PostStats postStats = new MypageResponseDto.PostStats(articleCount, commentCount);
        MypageResponseDto.Item item = new MypageResponseDto.Item(user.getNickname(), analysisStats, postStats);

        return new MypageResponseDto(List.of(item));
    }

    /** 내가 쓴 게시글 목록 조회 (페이지네이션) */
    public MypageBoardListResponseDto getMyBoards(SessionUser sessionUser, int page) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        PageRequest pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Board> boardPage = boardRepository.findByUserIdOrderByCreatedAtDesc(sessionUser.getId(), pageable);

        List<MypageBoardListResponseDto.Item> items = boardPage.getContent().stream()
                .map(b -> new MypageBoardListResponseDto.Item(b.getId(), b.getTitle(), b.getCreatedAt()))
                .collect(Collectors.toList());

        return new MypageBoardListResponseDto(boardPage.getTotalElements(), items);
    }

    /** 내가 쓴 댓글 목록 조회 (페이지네이션, 삭제된 댓글 제외) */
    public MypageCommentListResponseDto getMyComments(SessionUser sessionUser, int page) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        PageRequest pageable = PageRequest.of(page, PAGE_SIZE);
        // board fetch join으로 N+1 방지
        Page<Comment> commentPage = commentRepository.findByUserIdWithBoard(sessionUser.getId(), pageable);

        List<MypageCommentListResponseDto.Item> items = commentPage.getContent().stream()
                .map(c -> new MypageCommentListResponseDto.Item(
                        c.getBoard().getId(),
                        c.getBoard().getTitle(),
                        c.getContent(),
                        c.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new MypageCommentListResponseDto(commentPage.getTotalElements(), items);
    }

    /** 닉네임 변경 - 변경된 닉네임 반환 */
    @Transactional
    public String updateNickname(SessionUser sessionUser, NicknameUpdateRequestDto dto) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        // 중복 닉네임 체크 (본인 닉네임 제외)
        userRepository.findByNickname(dto.getNickname())
                .filter(u -> !u.getId().equals(sessionUser.getId()))
                .ifPresent(u -> { throw new BusinessException(ErrorType.DUPLICATE_NICKNAME); });

        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorType.USER_NOT_FOUND));

        user.updateNickname(dto.getNickname());
        return user.getNickname();
    }

    /** 회원 탈퇴 (소프트 삭제 + 세션 무효화) */
    @Transactional
    public void withdraw(SessionUser sessionUser, HttpSession session) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorType.USER_NOT_FOUND));

        user.withdraw();
        session.invalidate();
    }
}
