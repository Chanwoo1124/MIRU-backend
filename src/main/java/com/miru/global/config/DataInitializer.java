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

import java.util.ArrayList;
import java.util.List;

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

        // ── 테스트 유저 10명 생성 ──
        List<User> users = new ArrayList<>();
        String[] nicknames = {"김철수", "이영희", "박민준", "최지은", "정우성", "한소희", "오태양", "임수진", "강동원", "윤아름"};
        for (int i = 0; i < 10; i++) {
            // 첫 번째 유저(김철수)는 관리자로 생성
            Role role = (i == 0) ? Role.ADMIN : Role.USER;
            User u = User.builder()
                    .email("test" + (i + 1) + "@miru.com")
                    .nickname(nicknames[i])
                    .loginFrom("google")
                    .loginFromId("test-google-id-" + (i + 1))
                    .role(role)
                    .build();
            users.add(userRepository.save(u));
        }
        User adminUser = users.get(0);

        // ── 자기분석 질문 40개 생성 ──
        String[] questions = {
                "자기소개를 부탁드립니다.",
                "자기PR을 해주세요.",
                "본인을 한마디로 표현하면 어떤 사람입니까?",
                "당신의 강점은 무엇입니까?",
                "당신의 단점은 무엇입니까?",
                "주변 사람들은 당신을 어떤 사람이라고 평가합니까?",
                "학생 시절 가장 힘쓴 것은 무엇입니까?",
                "가장 큰 성취 경험은 무엇입니까?",
                "실패 또는 좌절 경험을 말씀해주세요.",
                "가장 아쉬웠던 경험은 무엇입니까?",
                "단체 생활에서 당신은 어떤 역할입니까?",
                "팀에 기여한 경험을 말씀해주세요.",
                "갈등을 해결한 경험을 말씀해주세요.",
                "리더십을 발휘한 경험이 있습니까?",
                "책임감을 발휘했던 경험은 무엇입니까?",
                "변화를 만들어낸 경험이 있습니까?",
                "어려운 상황을 극복한 경험을 말씀해주세요.",
                "스트레스를 받을 때 어떻게 대처합니까?",
                "일을 할 때 중요하게 생각하는 가치관은 무엇입니까?",
                "회사를 고를 때 가장 중요하게 보는 기준은 무엇입니까?",
                "왜 이 직종을 지원했습니까?",
                "왜 다른 직무가 아니라 이 직무입니까?",
                "왜 우리 회사를 지망했습니까?",
                "왜 다른 회사가 아니라 우리 회사입니까?",
                "왜 일본취업입니까?",
                "한국이 아니라 일본이어야 하는 이유는 무엇입니까?",
                "일본취업을 위해 지금까지 어떤 준비를 해왔습니까?",
                "일본 회사 문화에 잘 적응할 수 있다고 생각하는 이유는 무엇입니까?",
                "5년 후, 10년 후 어떤 커리어를 그리고 있습니까?",
                "입사 후 어떤 사람이 되고 싶습니까?",
                "지금까지 가장 몰입했던 경험은 무엇입니까?",
                "당신이 꾸준히 노력해온 것은 무엇입니까?",
                "스스로 성장했다고 느낀 경험은 무엇입니까?",
                "자신의 행동이나 생각이 크게 바뀐 계기가 있었습니까?",
                "협업할 때 가장 중요하다고 생각하는 것은 무엇입니까?",
                "다른 사람과 의견이 다를 때 어떻게 행동합니까?",
                "본인이 일하기 좋은 환경은 어떤 환경입니까?",
                "반대로 본인이 어려움을 느끼는 환경은 어떤 환경입니까?",
                "당신이 생각하는 사회인에게 가장 중요한 태도는 무엇입니까?",
                "회사에서 어떤 방식으로 기여하고 싶습니까?"
        };
        for (int i = 0; i < questions.length; i++) {
            questionRepository.save(new Question(questions[i], i + 1));
        }

        // ── 공지 게시글 5개 ──
        String[][] notices = {
                {"[공지] Miru 서비스 이용 안내", "안녕하세요. Miru 서비스 이용 안내입니다. 자기분석 페이지에서 40개의 질문에 답변하며 자신을 돌아보세요."},
                {"[공지] 커뮤니티 이용 규칙 안내", "건전한 커뮤니티 문화를 위해 욕설, 비방, 광고성 게시글은 삭제될 수 있습니다. 서로 존중하는 분위기를 만들어 주세요."},
                {"[공지] 서비스 점검 안내 (3/10)", "3월 10일 새벽 2시~4시 서버 점검이 예정되어 있습니다. 이용에 불편을 드려 죄송합니다."},
                {"[공지] 신규 기능 업데이트 안내", "자기분석 결과 공유 기능이 추가되었습니다. 프로필 페이지에서 확인해보세요."},
                {"[공지] 개인정보처리방침 개정 안내", "2026년 3월 1일부로 개인정보처리방침이 일부 개정되었습니다. 상세 내용을 확인해 주세요."}
        };
        for (String[] notice : notices) {
            boardRepository.save(Board.builder()
                    .user(adminUser)
                    .title(notice[0])
                    .content(notice[1])
                    .type(BoardType.NOTICE)
                    .build());
        }

        // ── 일반 게시글 100개 생성용 제목/내용 풀 ──
        String[] titlePool = {
                "스프링 부트 공부 방법 공유합니다",
                "자기소개서 작성 팁 정리",
                "면접 준비 후기 솔직하게",
                "JPA N+1 문제 해결법",
                "취업 준비 마음가짐",
                "리액트와 스프링 연동 삽질기",
                "Git 브랜치 전략 어떻게 하세요?",
                "포트폴리오 사이트 만들기 후기",
                "CS 기초 공부 어디서부터 시작?",
                "알고리즘 꾸준히 하는 방법",
                "Docker 처음 써봤는데 신기하네요",
                "AWS EC2 배포 삽질 정리",
                "코드 리뷰 문화 어떻게 만드나요?",
                "개발자 번아웃 극복 경험 공유",
                "ORM vs 순수 SQL 언제 무엇을?",
                "Redis 캐싱 적용 후기",
                "MSA 전환 고려 중인데 조언 구합니다",
                "Kafka 처음 도입했는데 도움이 됐어요",
                "TypeScript 마이그레이션 후기",
                "CI/CD 파이프라인 구축 경험",
                "테스트 코드 작성 습관 들이기",
                "인턴 경험 공유합니다",
                "토이 프로젝트 아이디어 추천",
                "개발 관련 유튜브 채널 추천",
                "기술 블로그 운영 팁",
                "노션으로 개발 일정 관리하기",
                "페어 프로그래밍 해보셨나요?",
                "스터디 구성원 모집 후기",
                "사이드 프로젝트 수익화 경험",
                "개발자 커뮤니케이션 스킬 중요성",
                "신입 개발자로 살아남기",
                "레거시 코드 리팩토링 경험담",
                "코드 품질 측정 도구 추천",
                "Spring Security 설정 헷갈리는 분",
                "JWT vs 세션 방식 비교 정리",
                "OAuth2 소셜 로그인 구현 후기",
                "멀티모듈 프로젝트 구성 방법",
                "Lombok 어노테이션 정리",
                "QueryDSL 입문 후기",
                "Swagger UI 세팅 쉽게 하기",
                "Kubernetes 입문 어렵지 않아요",
                "모니터링 시스템 Grafana 도입기",
                "로그 관리 ELK Stack 경험",
                "API 버저닝 전략 고민 중",
                "페이지네이션 vs 무한스크롤",
                "검색 기능 Elasticsearch 적용기",
                "이메일 알림 기능 구현 팁",
                "파일 업로드 S3 연동 후기",
                "WebSocket 채팅 구현 경험",
                "GraphQL 써볼 만한가요?",
                "Kotlin으로 전환 고민 중입니다",
                "함수형 프로그래밍 개념 정리",
                "디자인 패턴 공부 방법",
                "클린 아키텍처 적용 후기",
                "DDD 도메인 주도 설계 입문",
                "CQRS 패턴 실무 적용기",
                "이벤트 소싱 개념 정리",
                "마이크로서비스 통신 방법 정리",
                "gRPC 처음 써봤는데 빠르네요",
                "성능 테스트 JMeter 활용기",
                "코드 보안 OWASP Top 10 정리",
                "SQL 인덱스 최적화 경험",
                "트랜잭션 격리 수준 정리",
                "데이터베이스 샤딩 개념 공부",
                "인메모리 DB 언제 쓸까요?",
                "NoSQL 언제 선택해야 할까",
                "MongoDB 처음 써본 후기",
                "Firebase 간단 프로젝트에 좋아요",
                "Supabase 써보셨나요?",
                "Vercel 배포 너무 편해요",
                "Netlify vs Vercel 비교",
                "GitHub Actions 자동화 경험",
                "Terraform으로 인프라 관리",
                "코드 포매터 팀 표준 정하기",
                "ESLint Prettier 설정 공유",
                "모노레포 구성 경험",
                "패키지 매니저 pnpm 써보세요",
                "Vite vs Webpack 성능 비교",
                "SSR vs CSR 어떻게 선택?",
                "Next.js 사용 후기",
                "Nuxt.js 써본 분 계세요?",
                "SvelteKit 입문 후기",
                "Astro 정적 사이트 후기",
                "Tailwind CSS 처음엔 낯설어요",
                "CSS-in-JS vs CSS Modules",
                "Storybook 컴포넌트 문서화",
                "접근성(a11y) 고려한 개발 팁",
                "반응형 디자인 구현 팁",
                "다크모드 구현 경험 공유",
                "웹 성능 최적화 실전 팁",
                "Core Web Vitals 개선 경험",
                "PWA 오프라인 기능 구현",
                "웹 푸시 알림 구현 후기",
                "크로스 브라우저 호환성 대응",
                "SEO 최적화 경험 공유",
                "서버리스 아키텍처 도입기",
                "오픈소스 기여 처음 해봤어요",
                "개발자 영어 공부 방법",
                "개발 서적 추천 목록"
        };

        String[] contentPool = {
                "공부하면서 느낀 점을 공유합니다. 처음에는 막막했지만 꾸준히 하다 보니 조금씩 나아지고 있어요. 같이 고민하는 분들께 도움이 됐으면 좋겠습니다.",
                "실제로 경험해보니 생각했던 것과 많이 달랐습니다. 어려운 부분도 있었지만 결국 해결했고 그 과정에서 많이 배웠어요. 저처럼 헤매는 분들을 위해 정리해봤습니다.",
                "이 주제로 고민하시는 분들이 많은 것 같아 공유해드립니다. 제 경험이 정답은 아니지만 참고하시면 도움이 될 것 같습니다.",
                "처음 시작할 때 막막했던 기억이 납니다. 시행착오를 거치면서 나름의 방법을 찾았는데 혹시 더 좋은 방법 아시는 분 계시면 댓글로 알려주세요.",
                "요즘 이 부분을 많이 고민하고 있는데 여러분들의 경험도 궁금합니다. 댓글로 의견 나눠요!",
                "팀 프로젝트에서 이 문제를 겪었고 해결책을 찾기까지 꽤 오랜 시간이 걸렸습니다. 같은 상황에 계신 분들께 도움이 됐으면 좋겠어요.",
                "개인 프로젝트에서 적용해보면서 장단점을 정리했습니다. 완벽하지 않지만 시작점이 됐으면 합니다.",
                "인터넷에 자료가 많지 않아서 직접 삽질한 내용을 정리했습니다. 더 좋은 방법 아시면 공유해주세요!",
                "취준하면서 배운 내용입니다. 아직 부족한 점이 많지만 같이 성장해가요.",
                "실무에서 적용해본 경험을 공유합니다. 이론과 실제는 다르더라고요. 여러분의 경험도 듣고 싶어요."
        };

        // ── 일반 게시글 100개 생성 ──
        List<Board> boards = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            User author = users.get(i % users.size());
            String title = titlePool[i % titlePool.length] + (i >= titlePool.length ? " (" + (i / titlePool.length + 1) + ")" : "");
            String content = contentPool[i % contentPool.length];

            Board board = boardRepository.save(Board.builder()
                    .user(author)
                    .title(title)
                    .content(content)
                    .type(BoardType.GENERAL)
                    .build());
            boards.add(board);
        }

        // ── 댓글 20개 + 대댓글 5개씩 생성 ──
        String[] commentPool = {
                "좋은 글 감사합니다! 많이 배웠어요.",
                "저도 같은 고민을 했었는데 공감돼요.",
                "이런 내용 더 써주세요! 항상 도움이 돼요.",
                "혹시 관련 레퍼런스도 공유해주실 수 있나요?",
                "실제로 적용해봤는데 잘 되더라고요. 감사해요!",
                "오 저도 이 방법 써봐야겠다 ㅎㅎ",
                "몰랐던 내용인데 새로 알게 됐어요. 고마워요!",
                "질문이 있는데요, 혹시 DM 드려도 될까요?",
                "북마크 했어요! 두고두고 봐야겠네요.",
                "다음 글도 기대하겠습니다 :)",
                "저도 비슷한 경험이 있어요. 공감 100%",
                "이런 글 자주 올려주세요. 커뮤니티가 좋아지네요.",
                "처음 접하는 개념이었는데 이해하기 쉽게 설명해주셨네요.",
                "팀원한테도 공유했어요. 다들 도움 됐다고 하더라고요.",
                "글 잘 읽었습니다. 추가로 궁금한 게 있어서요...",
                "제 상황이랑 딱 맞는 글이에요. 타이밍 좋게 올려주셨네요!",
                "비슷한 시행착오를 겪었는데 이렇게 정리해주시니 좋네요.",
                "저는 다른 방법을 사용하는데 한번 이 방법도 시도해볼게요.",
                "댓글로도 추가 질문드려도 될까요? ㅎㅎ",
                "진짜 유익한 글이에요. 저장해뒀어요!"
        };

        String[] replyPool = {
                "맞아요, 저도 처음에 헤맸어요 ㅎㅎ",
                "감사해요! 더 궁금하신 거 있으면 질문해주세요.",
                "ㅋㅋㅋ 공감합니다. 다들 비슷한 경험을 하는군요.",
                "오 그렇군요. 저는 몰랐는데 알려주셔서 감사해요!",
                "맞아요 그 부분이 좀 어렵더라고요.",
                "DM 환영이에요! 편하게 연락주세요.",
                "저도 같은 생각이에요.",
                "추가로 이런 자료도 있어요! 참고해보세요.",
                "이 방법 말고도 대안이 있는데 상황에 따라 선택하면 좋을 것 같아요.",
                "좋은 질문이에요! 저도 더 공부해볼게요."
        };

        for (Board board : boards) {
            int commentCount = 0;

            // 댓글 20개 생성
            for (int c = 0; c < 20; c++) {
                User commentUser = users.get(c % users.size());
                Comment comment = commentRepository.save(Comment.builder()
                        .board(board)
                        .user(commentUser)
                        .context(commentPool[c % commentPool.length])
                        .parent(null)
                        .build());
                commentCount++;

                // 대댓글 5개 생성
                for (int r = 0; r < 5; r++) {
                    User replyUser = users.get((c + r + 1) % users.size());
                    commentRepository.save(Comment.builder()
                            .board(board)
                            .user(replyUser)
                            .context(replyPool[r % replyPool.length])
                            .parent(comment)
                            .build());
                    commentCount++;
                }
            }

            // commentCount 반영 (20 + 20*5 = 120)
            for (int i = 0; i < commentCount; i++) {
                board.incrementCommentCount();
            }
            boardRepository.save(board);
        }
    }
}
