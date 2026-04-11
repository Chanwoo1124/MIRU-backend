package com.miru.domain.inquiry.repository;

import com.miru.domain.inquiry.entity.InquiryAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 문의 답변 레포지토리
 *
 * <p>관리자가 등록하는 문의 답변을 저장한다.
 * 답변 삭제는 InquiryBoard.clearAnswer() + orphanRemoval로 처리하므로
 * 직접 delete 쿼리를 사용하지 않는다.
 */
public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {
}
