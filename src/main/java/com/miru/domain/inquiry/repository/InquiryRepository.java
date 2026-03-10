package com.miru.domain.inquiry.repository;

import com.miru.domain.inquiry.entity.InquiryBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InquiryRepository extends JpaRepository<InquiryBoard, Long> {

    /** 특정 유저의 문의 전체 조회 - 최신순 */
    List<InquiryBoard> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    /** 전체 문의 조회 - user fetch join으로 N+1 방지, 최신순 */
    @Query("SELECT i FROM InquiryBoard i JOIN FETCH i.user ORDER BY i.createdAt DESC")
    List<InquiryBoard> findAllWithUserOrderByCreatedAtDesc();
}
