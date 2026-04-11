package com.miru.domain.analysis.repository;

import com.miru.domain.analysis.entity.Answer;
import com.miru.domain.analysis.entity.AnswerStatus;
import com.miru.domain.analysis.entity.Question;
import com.miru.domain.user.entity.Role;
import com.miru.domain.user.entity.User;
import com.miru.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Analysis 도메인 Repository 통합 테스트
 * H2 인메모리 DB를 사용하여 커스텀 JPQL 쿼리의 정확성을 검증한다.
 */
@DataJpaTest
@ActiveProfiles("test")
class AnalysisRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private Question question1;
    private Question question2;
    private Question question3;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 저장
        user = userRepository.save(User.builder()
                .email("user@test.com")
                .nickname("테스터")
                .loginFrom("google")
                .loginFromId("google-1")
                .role(Role.USER)
                .build());
        user.activate();

        // 질문 3개 저장 (orderId 순서 혼합)
        question1 = questionRepository.save(new Question("나는 어떤 성격인가?", 1));
        question2 = questionRepository.save(new Question("나의 강점은?", 2));
        question3 = questionRepository.save(new Question("나의 약점은?", 3));

        em.flush();
        em.clear();
    }

    // ===================== QuestionRepository =====================

    @Test
    @DisplayName("findAllByOrderByOrderIdAsc: 질문이 orderId 오름차순으로 반환된다")
    void findAllByOrderByOrderIdAsc_순서정렬() {
        // when
        List<Question> questions = questionRepository.findAllByOrderByOrderIdAsc();

        // then
        assertThat(questions).hasSize(3);
        assertThat(questions.get(0).getOrderId()).isEqualTo(1);
        assertThat(questions.get(1).getOrderId()).isEqualTo(2);
        assertThat(questions.get(2).getOrderId()).isEqualTo(3);
    }

    @Test
    @DisplayName("findAllWithUserAnswer: 답변이 없는 질문도 LEFT JOIN으로 포함 조회된다")
    void findAllWithUserAnswer_미답변질문포함() {
        // given - question1에만 답변 저장
        Answer answer = answerRepository.save(Answer.builder()
                .user(user)
                .question(question1)
                .content("나는 긍정적인 성격입니다.")
                .status(AnswerStatus.COMPLETED)
                .build());
        em.flush();
        em.clear();

        // when - 전체 질문 3개 + 유저 답변 LEFT JOIN
        List<Object[]> results = questionRepository.findAllWithUserAnswer(user.getId());

        // then - 질문이 3개이므로 결과도 3개 (미답변 질문은 answer = null)
        assertThat(results).hasSize(3);

        // 첫 번째 결과: question1 + answer
        Question q1 = (Question) results.get(0)[0];
        Answer a1 = (Answer) results.get(0)[1];
        assertThat(q1.getOrderId()).isEqualTo(1);
        assertThat(a1).isNotNull();
        assertThat(a1.getContent()).isEqualTo("나는 긍정적인 성격입니다.");

        // 두 번째 결과: question2 + null (미답변)
        Answer a2 = (Answer) results.get(1)[1];
        assertThat(a2).isNull();

        // 세 번째 결과: question3 + null (미답변)
        Answer a3 = (Answer) results.get(2)[1];
        assertThat(a3).isNull();
    }

    @Test
    @DisplayName("findAllWithUserAnswer: 다른 유저의 답변은 포함되지 않는다")
    void findAllWithUserAnswer_타유저답변제외() {
        // given - 다른 유저의 답변 저장
        User otherUser = userRepository.save(User.builder()
                .email("other@test.com")
                .nickname("다른유저")
                .loginFrom("naver")
                .loginFromId("naver-1")
                .role(Role.USER)
                .build());
        otherUser.activate();

        answerRepository.save(Answer.builder()
                .user(otherUser) // 다른 유저의 답변
                .question(question1)
                .content("다른 유저의 답변")
                .status(AnswerStatus.IN_PROGRESS)
                .build());
        em.flush();
        em.clear();

        // when - user(내 유저)의 답변 조회
        List<Object[]> results = questionRepository.findAllWithUserAnswer(user.getId());

        // then - user의 답변은 없으므로 모두 null
        for (Object[] row : results) {
            assertThat(row[1]).isNull();
        }
    }

    // ===================== AnswerRepository =====================

    @Test
    @DisplayName("findByUserIdAndQuestionId: 유저+질문 조합으로 정확히 조회된다")
    void findByUserIdAndQuestionId_정확조회() {
        // given
        Answer answer = answerRepository.save(Answer.builder()
                .user(user)
                .question(question2)
                .content("나의 강점은 집중력입니다.")
                .status(AnswerStatus.COMPLETED)
                .build());
        em.flush();
        em.clear();

        // when
        Optional<Answer> result = answerRepository.findByUserIdAndQuestionId(user.getId(), question2.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getContent()).isEqualTo("나의 강점은 집중력입니다.");
        assertThat(result.get().getStatus()).isEqualTo(AnswerStatus.COMPLETED);
    }

    @Test
    @DisplayName("findByUserIdAndQuestionId: 답변이 없으면 Optional.empty() 반환")
    void findByUserIdAndQuestionId_없으면빈값() {
        // when - 저장하지 않은 question3 조회
        Optional<Answer> result = answerRepository.findByUserIdAndQuestionId(user.getId(), question3.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteByUserIdAndQuestionId: 특정 유저의 특정 질문 답변만 삭제된다")
    void deleteByUserIdAndQuestionId_정확삭제() {
        // given
        answerRepository.save(Answer.builder()
                .user(user)
                .question(question1)
                .content("답변1")
                .status(AnswerStatus.IN_PROGRESS)
                .build());
        answerRepository.save(Answer.builder()
                .user(user)
                .question(question2)
                .content("답변2")
                .status(AnswerStatus.COMPLETED)
                .build());
        em.flush();
        em.clear();

        // when - question1의 답변만 삭제
        answerRepository.deleteByUserIdAndQuestionId(user.getId(), question1.getId());
        em.flush();
        em.clear();

        // then - question1 답변은 없고 question2 답변은 남아있어야 함
        assertThat(answerRepository.findByUserIdAndQuestionId(user.getId(), question1.getId())).isEmpty();
        assertThat(answerRepository.findByUserIdAndQuestionId(user.getId(), question2.getId())).isPresent();
    }

    @Test
    @DisplayName("countByUserIdAndStatus: 상태별 답변 수가 정확히 집계된다")
    void countByUserIdAndStatus_상태별집계() {
        // given
        answerRepository.save(Answer.builder()
                .user(user).question(question1).content("a").status(AnswerStatus.COMPLETED).build());
        answerRepository.save(Answer.builder()
                .user(user).question(question2).content("b").status(AnswerStatus.IN_PROGRESS).build());
        em.flush();

        // when
        long completedCount = answerRepository.countByUserIdAndStatus(user.getId(), AnswerStatus.COMPLETED);
        long inProgressCount = answerRepository.countByUserIdAndStatus(user.getId(), AnswerStatus.IN_PROGRESS);

        // then
        assertThat(completedCount).isEqualTo(1);
        assertThat(inProgressCount).isEqualTo(1);
    }
}
