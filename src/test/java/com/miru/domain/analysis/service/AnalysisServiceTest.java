package com.miru.domain.analysis.service;

import com.miru.domain.analysis.dto.AnswerSaveRequestDto;
import com.miru.domain.analysis.dto.AnswerSaveResponseDto;
import com.miru.domain.analysis.dto.QuestionListResponseDto;
import com.miru.domain.analysis.entity.Answer;
import com.miru.domain.analysis.entity.AnswerStatus;
import com.miru.domain.analysis.entity.Question;
import com.miru.domain.analysis.repository.AnswerRepository;
import com.miru.domain.analysis.repository.QuestionRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * AnalysisService 단위 테스트
 * Repository는 Mockito로 mock하여 서비스 비즈니스 로직만 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AnalysisService analysisService;

    private User user;
    private Question question;
    private SessionUser sessionUser;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 생성 (ACTIVE 상태)
        user = User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .loginFrom("google")
                .loginFromId("google-id-123")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        // activate() 호출로 ACTIVE 상태 전환
        user.activate();

        // 테스트용 질문 생성
        question = new Question("나는 어떤 사람인가?", 1);
        ReflectionTestUtils.setField(question, "id", 1L);

        sessionUser = new SessionUser(user);
    }

    // ===================== getQuestions =====================

    @Nested
    @DisplayName("getQuestions - 자기분석 질문 목록 조회")
    class GetQuestionsTest {

        @Test
        @DisplayName("비로그인 유저: answerContext와 status가 null로 반환된다")
        void getQuestions_비로그인() {
            // given
            given(questionRepository.findAllByOrderByOrderIdAsc()).willReturn(List.of(question));

            // when
            QuestionListResponseDto result = analysisService.getQuestions(null);

            // then
            assertThat(result.getTotalCount()).isEqualTo(1);
            assertThat(result.getItems().get(0).getAnswerContext()).isNull();
            assertThat(result.getItems().get(0).getStatus()).isNull();
        }

        @Test
        @DisplayName("로그인 유저: 질문과 답변이 함께 반환된다")
        void getQuestions_로그인() {
            // given
            Answer answer = Answer.builder()
                    .user(user)
                    .question(question)
                    .content("나는 긍정적인 사람입니다.")
                    .status(AnswerStatus.COMPLETED)
                    .build();
            // Object[] 배열로 반환하는 JPQL 결과 시뮬레이션 (List<Object[]> 타입 명시)
            List<Object[]> rows = new ArrayList<>();
            rows.add(new Object[]{question, answer});
            given(questionRepository.findAllWithUserAnswer(1L)).willReturn(rows);

            // when
            QuestionListResponseDto result = analysisService.getQuestions(sessionUser);

            // then
            assertThat(result.getTotalCount()).isEqualTo(1);
            assertThat(result.getItems().get(0).getAnswerContext()).isEqualTo("나는 긍정적인 사람입니다.");
            assertThat(result.getItems().get(0).getStatus()).isEqualTo(AnswerStatus.COMPLETED);
        }

        @Test
        @DisplayName("로그인 유저이지만 미답변 질문: answerContext와 status가 null이다")
        void getQuestions_로그인_미답변() {
            // given - Object[1]이 null인 경우 (LEFT JOIN에서 answer가 없는 경우)
            List<Object[]> rows = new ArrayList<>();
            rows.add(new Object[]{question, null});
            given(questionRepository.findAllWithUserAnswer(1L)).willReturn(rows);

            // when
            QuestionListResponseDto result = analysisService.getQuestions(sessionUser);

            // then
            assertThat(result.getItems().get(0).getAnswerContext()).isNull();
            assertThat(result.getItems().get(0).getStatus()).isNull();
        }
    }

    // ===================== saveAnswer =====================

    @Nested
    @DisplayName("saveAnswer - 답변 저장/수정")
    class SaveAnswerTest {

        private AnswerSaveRequestDto dto;

        @BeforeEach
        void setUp() {
            // @NoArgsConstructor + ReflectionTestUtils로 private 필드 설정
            dto = new AnswerSaveRequestDto();
            ReflectionTestUtils.setField(dto, "answerContext", "나는 도전적인 사람입니다.");
            ReflectionTestUtils.setField(dto, "status", AnswerStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("신규 답변 저장: 기존 답변 없으면 save() 호출")
        void saveAnswer_신규저장() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(questionRepository.findById(1L)).willReturn(Optional.of(question));
            given(answerRepository.findByUserIdAndQuestionId(1L, 1L)).willReturn(Optional.empty());

            // when
            AnswerSaveResponseDto result = analysisService.saveAnswer(1L, sessionUser, dto);

            // then
            verify(answerRepository, times(1)).save(any(Answer.class));
            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getItems().get(0).getAnswerContext()).isEqualTo("나는 도전적인 사람입니다.");
        }

        @Test
        @DisplayName("기존 답변 수정: 이미 답변이 있으면 update() 호출하고 save() 미호출")
        void saveAnswer_기존수정() {
            // given
            Answer existingAnswer = Answer.builder()
                    .user(user)
                    .question(question)
                    .content("기존 내용")
                    .status(AnswerStatus.IN_PROGRESS)
                    .build();
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(questionRepository.findById(1L)).willReturn(Optional.of(question));
            given(answerRepository.findByUserIdAndQuestionId(1L, 1L)).willReturn(Optional.of(existingAnswer));

            // when
            AnswerSaveResponseDto result = analysisService.saveAnswer(1L, sessionUser, dto);

            // then
            // 기존 답변 수정이므로 save()는 호출되지 않아야 함
            verify(answerRepository, never()).save(any(Answer.class));
            // 내용이 변경됐는지 확인
            assertThat(existingAnswer.getContent()).isEqualTo("나는 도전적인 사람입니다.");
        }

        @Test
        @DisplayName("비로그인 유저: UNAUTHORIZED 예외 발생")
        void saveAnswer_비로그인() {
            assertThatThrownBy(() -> analysisService.saveAnswer(1L, null, dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.UNAUTHORIZED);
        }

        @Test
        @DisplayName("존재하지 않는 질문: QUESTION_NOT_FOUND 예외 발생")
        void saveAnswer_질문없음() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(questionRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> analysisService.saveAnswer(999L, sessionUser, dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.QUESTION_NOT_FOUND);
        }
    }

    // ===================== deleteAnswer =====================

    @Nested
    @DisplayName("deleteAnswer - 답변 삭제")
    class DeleteAnswerTest {

        @Test
        @DisplayName("답변 삭제 성공: 빈 items 반환")
        void deleteAnswer_성공() {
            // given
            Answer answer = Answer.builder()
                    .user(user)
                    .question(question)
                    .content("삭제할 내용")
                    .status(AnswerStatus.IN_PROGRESS)
                    .build();
            given(questionRepository.findById(1L)).willReturn(Optional.of(question));
            given(answerRepository.findByUserIdAndQuestionId(1L, 1L)).willReturn(Optional.of(answer));

            // when
            AnswerSaveResponseDto result = analysisService.deleteAnswer(1L, sessionUser);

            // then
            verify(answerRepository, times(1)).deleteByUserIdAndQuestionId(1L, 1L);
            assertThat(result.getItems()).isEmpty();
        }

        @Test
        @DisplayName("비로그인 유저: UNAUTHORIZED 예외 발생")
        void deleteAnswer_비로그인() {
            assertThatThrownBy(() -> analysisService.deleteAnswer(1L, null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.UNAUTHORIZED);
        }

        @Test
        @DisplayName("존재하지 않는 답변: ANSWER_NOT_FOUND 예외 발생")
        void deleteAnswer_답변없음() {
            // given
            given(questionRepository.findById(1L)).willReturn(Optional.of(question));
            given(answerRepository.findByUserIdAndQuestionId(1L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> analysisService.deleteAnswer(1L, sessionUser))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.ANSWER_NOT_FOUND);
        }
    }
}
