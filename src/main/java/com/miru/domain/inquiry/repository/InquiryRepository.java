package com.miru.domain.inquiry.repository;

import com.miru.domain.inquiry.entity.InquiryBoard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<InquiryBoard, Long> {

    /** 특정 유저의 문의 전체 조회 - 최신순 */
    List<InquiryBoard> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
