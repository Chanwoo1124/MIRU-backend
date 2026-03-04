package com.miru.domain.analysis.service;

import com.miru.domain.analysis.dto.AnswerSaveRequestDto;
import com.miru.domain.analysis.dto.AnswerSaveResponseDto;
import com.miru.domain.analysis.dto.QuestionListResponseDto;
import com.miru.domain.analysis.entity.Answer;
import com.miru.domain.analysis.entity.AnswerStatus;
import com.miru.domain.analysis.entity.Question;
import com.miru.domain.analysis.repository.AnswerRepository;
import com.miru.domain.analysis.repository.QuestionRepository;
import com.miru.domain.user.entity.User;
import com.miru.domain.user.repository.UserRepository;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.error.BusinessException;
import com.miru.global.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    /**
     * 자기분석 질문 목록 조회
     * 로그인 시 답변 포함, 비로그인 시 answerContext/status null
     */
    public QuestionListResponseDto getQuestions(SessionUser sessionUser) {
        if (sessionUser == null) {
            // 비로그인: 질문만 조회
            List<Question> questions = questionRepository.findAllByOrderByOrderIdAsc();
            List<QuestionListResponseDto.Item> items = questions.stream()
                    .map(q -> new QuestionListResponseDto.Item(q.getId(), q.getContent(), null, null))
                    .collect(Collectors.toList());
            return new QuestionListResponseDto(items.size(), items);
        }

        // 로그인: 질문 + 답변 LEFT JOIN 조회
        List<Object[]> results = questionRepository.findAllWithUserAnswer(sessionUser.getId());
        List<QuestionListResponseDto.Item> items = results.stream()
                .map(row -> {
                    Question question = (Question) row[0];
                    Answer answer = (Answer) row[1];
                    String answerContext = answer != null ? answer.getContent() : null;
                    AnswerStatus status = answer != null ? answer.getStatus() : null;
                    return new QuestionListResponseDto.Item(question.getId(), question.getContent(), answerContext, status);
                })
                .collect(Collectors.toList());

        return new QuestionListResponseDto(items.size(), items);
    }

    /** 답변 저장 및 수정 (이미 있으면 수정, 없으면 생성) */
    @Transactional
    public AnswerSaveResponseDto saveAnswer(Long questionId, SessionUser sessionUser, AnswerSaveRequestDto dto) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorType.USER_NOT_FOUND));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorType.QUESTION_NOT_FOUND));

        Optional<Answer> existing = answerRepository.findByUserIdAndQuestionId(sessionUser.getId(), questionId);

        if (existing.isPresent()) {
            // 기존 답변 수정
            existing.get().update(dto.getAnswerContext(), dto.getStatus());
        } else {
            // 새 답변 저장
            Answer answer = Answer.builder()
                    .user(user)
                    .question(question)
                    .content(dto.getAnswerContext())
                    .status(dto.getStatus())
                    .build();
            answerRepository.save(answer);
        }

        AnswerSaveResponseDto.Item item = new AnswerSaveResponseDto.Item(
                question.getId(), question.getContent(), dto.getAnswerContext(), dto.getStatus()
        );
        return new AnswerSaveResponseDto(List.of(item));
    }

    /** 답변 초기화 */
    @Transactional
    public AnswerSaveResponseDto deleteAnswer(Long questionId, SessionUser sessionUser) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorType.QUESTION_NOT_FOUND));

        answerRepository.findByUserIdAndQuestionId(sessionUser.getId(), questionId)
                .orElseThrow(() -> new BusinessException(ErrorType.ANSWER_NOT_FOUND));

        answerRepository.deleteByUserIdAndQuestionId(sessionUser.getId(), questionId);

        return new AnswerSaveResponseDto(Collections.emptyList());
    }
}
