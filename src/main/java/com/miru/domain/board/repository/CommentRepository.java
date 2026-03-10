package com.miru.domain.board.repository;

import com.miru.domain.board.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** 특정 게시글의 최상위 댓글 + 대댓글 fetch join 조회 (작성순) */
    @Query("SELECT DISTINCT c FROM Comment c LEFT JOIN FETCH c.replies WHERE c.board.id = :boardId AND c.parent IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findTopCommentsWithReplies(@Param("boardId") Long boardId);

    /** 특정 유저의 댓글 목록 조회 - 삭제되지 않은 것만, 최신순 (board fetch join으로 N+1 방지) */
    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.board WHERE c.user.id = :userId AND c.isDeleted = false ORDER BY c.createdAt DESC",
           countQuery = "SELECT COUNT(c) FROM Comment c WHERE c.user.id = :userId AND c.isDeleted = false")
    Page<Comment> findByUserIdWithBoard(@Param("userId") Long userId, Pageable pageable);

    /** 특정 유저의 삭제되지 않은 댓글 수 */
    long countByUserIdAndIsDeletedFalse(Long userId);
}