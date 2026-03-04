package com.miru.domain.analysis.repository;

import com.miru.domain.analysis.entity.Answer;
import com.miru.domain.analysis.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    /** 전체 질문 목록 조회 (순서 기준 오름차순) - 비로그인용 */
    List<Question> findAllByOrderByOrderIdAsc();

    /**
     * 전체 질문 + 유저 답변 LEFT JOIN 조회 (순서 기준 오름차순) - 로그인용
     * Object[0] = Question, Object[1] = Answer (미답변 시 null)
     */
    @Query("SELECT q, a FROM Question q LEFT JOIN Answer a ON a.question = q AND a.user.id = :userId ORDER BY q.orderId ASC")
    List<Object[]> findAllWithUserAnswer(@Param("userId") Long userId);
}
