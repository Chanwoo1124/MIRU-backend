package com.miru.domain.board.repository;

import com.miru.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {

    /**
     * 전체 게시글 조회 - NOTICE 타입 최상단 고정 후 최신순 정렬
     */
    @Query("SELECT b FROM Board b ORDER BY CASE WHEN b.type = 'NOTICE' THEN 0 ELSE 1 END ASC, b.createdAt DESC")
    Page<Board> findAllOrderByNoticeFirst(Pageable pageable);

    /**
     * 제목 키워드 검색 - 최신순 정렬
     */
    @Query("SELECT b FROM Board b WHERE b.title LIKE %:keyword% ORDER BY b.createdAt DESC")
    Page<Board> findByTitleContaining(@Param("keyword") String keyword, Pageable pageable);
}