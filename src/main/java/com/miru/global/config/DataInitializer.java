package com.miru.global.config;

import com.miru.domain.analysis.entity.Question;
import com.miru.domain.analysis.repository.QuestionRepository;
import com.miru.domain.board.entity.Board;
import com.miru.domain.board.entity.BoardType;
import com.miru.domain.board.entity.Comment;
import com.miru.domain.board.repository.BoardRepository;
import com.miru.domain.board.repository.CommentRepository;
import com.miru.domain.user.entity.Role;
import com.miru.domain.user.entity.User;
import com.miru.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final QuestionRepository questionRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (questionRepository.count() > 0) return; // 이미 데이터 있으면 스킵

        // 테스트 유저 생성
        User user = User.builder()
                .email("test@miru.com")
                .nickname("테스트유저")
                .loginFrom("google")
                .loginFromId("test-google-id")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // 자기분석 질문 30개 생성
        String[] questions = {
                "자신의 가장 큰 강점은 무엇인가요?",
                "자신의 단점이나 개선해야 할 점은 무엇인가요?",
                "살면서 가장 힘들었던 경험은 무엇인가요?",
                "그 경험을 어떻게 극복했나요?",
                "지금까지 가장 뿌듯했던 성취는 무엇인가요?",
                "10년 후 자신의 모습을 어떻게 그리고 있나요?",
                "가장 중요하게 생각하는 가치관은 무엇인가요?",
                "팀 프로젝트에서 주로 어떤 역할을 맡나요?",
                "갈등 상황에서 어떻게 행동하나요?",
                "새로운 환경에 적응하는 방식을 설명해 주세요.",
                "최근 1년 동안 가장 열심히 노력한 것은 무엇인가요?",
                "실패 경험에서 배운 점이 있다면 무엇인가요?",
                "자신을 동물에 비유한다면 어떤 동물인가요? 그 이유는?",
                "타인에게 인정받고 싶은 부분은 무엇인가요?",
                "스트레스를 해소하는 자신만의 방법이 있나요?",
                "지원하려는 직무에 관심을 갖게 된 계기는 무엇인가요?",
                "해당 직무에서 필요한 역량 중 자신 있는 것은 무엇인가요?",
                "관련 경험이나 프로젝트가 있다면 소개해 주세요.",
                "직무 수행 시 가장 중요하게 생각하는 것은 무엇인가요?",
                "입사 후 3년 안에 이루고 싶은 목표가 있나요?",
                "협업 경험 중 가장 기억에 남는 것은 무엇인가요?",
                "리더십을 발휘한 경험이 있나요?",
                "데드라인이 촉박한 상황에서 어떻게 일을 처리하나요?",
                "자기 계발을 위해 꾸준히 하는 활동이 있나요?",
                "지원하는 회사에 대해 알고 있는 것을 말해 주세요.",
                "우리 회사를 선택한 이유는 무엇인가요?",
                "다른 회사가 아닌 우리 회사여야 하는 이유는?",
                "첫 출근 날 가장 먼저 하고 싶은 일은 무엇인가요?",
                "회사 생활에서 가장 중요하게 생각하는 것은 무엇인가요?",
                "마지막으로 자신을 한 문장으로 표현해 주세요."
        };

        for (int i = 0; i < questions.length; i++) {
            questionRepository.save(new Question(questions[i], i + 1));
        }

        // 공지 게시글 1개
        boardRepository.save(Board.builder()
                .user(user)
                .title("[공지] Miru 서비스 이용 안내")
                .content("안녕하세요. Miru 서비스 이용 안내입니다. 자기분석 페이지에서 30개의 질문에 답변하며 자신을 돌아보세요.")
                .type(BoardType.NOTICE)
                .build());

        // 일반 게시글 5개
        String[][] boards = {
                {"스프링 부트 공부 방법 공유", "스프링 부트를 공부하면서 도움이 된 자료들을 공유합니다."},
                {"자기소개서 작성 팁", "자기소개서를 작성하면서 느낀 점들을 공유합니다."},
                {"면접 준비 후기", "최근 면접을 준비하면서 경험한 것들을 나눕니다."},
                {"JPA N+1 문제 해결법", "JPA를 사용하면서 마주친 N+1 문제와 해결 방법을 정리했습니다."},
                {"취업 준비 마음가짐", "취업 준비 중 멘탈 관리 방법에 대해 이야기해봅니다."}
        };

        // 일반 게시글 저장 (댓글 달 대상 게시글)
        Board targetBoard = null;
        for (int i = 0; i < boards.length; i++) {
            Board saved = boardRepository.save(Board.builder()
                    .user(user)
                    .title(boards[i][0])
                    .content(boards[i][1])
                    .type(BoardType.GENERAL)
                    .build());
            if (i == 0) targetBoard = saved; // 첫 번째 게시글에 댓글 추가
        }

        // 댓글 + 대댓글 목데이터 생성 (첫 번째 일반 게시글 기준)
        if (targetBoard != null) {
            // 댓글 1
            Comment comment1 = commentRepository.save(Comment.builder()
                    .board(targetBoard)
                    .user(user)
                    .context("저도 스프링 부트 공부 중인데 정말 도움이 됩니다!")
                    .parent(null)
                    .build());
            targetBoard.incrementCommentCount();

            // 댓글 1의 대댓글
            commentRepository.save(Comment.builder()
                    .board(targetBoard)
                    .user(user)
                    .context("저도 같은 방법으로 공부했어요 ㅎㅎ")
                    .parent(comment1)
                    .build());
            targetBoard.incrementCommentCount();

            commentRepository.save(Comment.builder()
                    .board(targetBoard)
                    .user(user)
                    .context("혹시 추천하는 강의도 있나요?")
                    .parent(comment1)
                    .build());
            targetBoard.incrementCommentCount();

            // 댓글 2
            Comment comment2 = commentRepository.save(Comment.builder()
                    .board(targetBoard)
                    .user(user)
                    .context("공식 문서도 같이 보시면 더 좋아요!")
                    .parent(null)
                    .build());
            targetBoard.incrementCommentCount();

            // 댓글 2의 대댓글
            commentRepository.save(Comment.builder()
                    .board(targetBoard)
                    .user(user)
                    .context("공식 문서가 생각보다 잘 되어 있더라고요")
                    .parent(comment2)
                    .build());
            targetBoard.incrementCommentCount();

            commentRepository.save(Comment.builder()
                    .board(targetBoard)
                    .user(user)
                    .context("영어가 약해서 번역본으로 보고 있어요 ㅠ")
                    .parent(comment2)
                    .build());
            targetBoard.incrementCommentCount();

            commentRepository.save(Comment.builder()
                    .board(targetBoard)
                    .user(user)
                    .context("DeepL 쓰면 번역이 꽤 자연스러워요!")
                    .parent(comment2)
                    .build());
            targetBoard.incrementCommentCount();

            // 댓글 3
            Comment comment3 = commentRepository.save(Comment.builder()
                    .board(targetBoard)
                    .user(user)
                    .context("게시글 감사합니다. 북마크 해뒀어요 👍")
                    .parent(null)
                    .build());
            targetBoard.incrementCommentCount();

            // 댓글 3의 대댓글
            commentRepository.save(Comment.builder()
                    .board(targetBoard)
                    .user(user)
                    .context("도움이 됐으면 좋겠네요!")
                    .parent(comment3)
                    .build());
            targetBoard.incrementCommentCount();

            boardRepository.save(targetBoard);
        }
    }
}
