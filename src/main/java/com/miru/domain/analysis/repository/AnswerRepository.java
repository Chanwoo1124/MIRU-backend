package com.miru.domain.analysis.repository;

import com.miru.domain.analysis.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    /** 특정 유저의 특정 질문 답변 조회 (저장 시 기존 답변 여부 확인용) */
    Optional<Answer> findByUserIdAndQuestionId(Long userId, Long questionId);

    /** 특정 유저의 특정 질문 답변 삭제 */
    void deleteByUserIdAndQuestionId(Long userId, Long questionId);
}
