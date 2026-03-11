package com.miru.domain.board.repository;

import com.miru.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {

    /** 특정 유저의 게시글 목록 조회 (최신순) */
    Page<Board> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** 특정 유저의 전체 게시글 수 */
    long countByUserId(Long userId);

    /**
     * 전체 게시글 조회 - NOTICE 타입 최상단 고정 후 최신순 정렬 (user fetch join으로 N+1 방지)
     */
    @Query(value = "SELECT b FROM Board b JOIN FETCH b.user ORDER BY CASE WHEN b.type = 'NOTICE' THEN 0 ELSE 1 END ASC, b.createdAt DESC",
           countQuery = "SELECT COUNT(b) FROM Board b")
    Page<Board> findAllOrderByNoticeFirst(Pageable pageable);

    /**
     * 제목 키워드 검색 - NOTICE 타입 최상단 고정 후 최신순 정렬 (user fetch join으로 N+1 방지)
     */
    @Query(value = "SELECT b FROM Board b JOIN FETCH b.user WHERE b.title LIKE %:keyword% ORDER BY CASE WHEN b.type = 'NOTICE' THEN 0 ELSE 1 END ASC, b.createdAt DESC",
           countQuery = "SELECT COUNT(b) FROM Board b WHERE b.title LIKE %:keyword%")
    Page<Board> findByTitleContaining(@Param("keyword") String keyword, Pageable pageable);
}