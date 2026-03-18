package com.miru.global.config;

import com.miru.domain.analysis.entity.Question;
import com.miru.domain.analysis.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final QuestionRepository questionRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (questionRepository.count() > 0) return; // 이미 데이터 있으면 스킵

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
    }
}
