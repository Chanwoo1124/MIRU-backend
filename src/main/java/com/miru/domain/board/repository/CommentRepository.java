package com.miru.domain.board.repository;

import com.miru.domain.board.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** 특정 게시글의 최상위 댓글 목록 조회 (작성순) */
    List<Comment> findByBoardIdAndParentIsNullOrderByCreatedAtAsc(Long boardId);

    /** 특정 댓글의 대댓글 목록 조회 (작성순) */
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    /** 특정 유저의 댓글 목록 조회 - 삭제되지 않은 것만, 최신순 */
    Page<Comment> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** 특정 유저의 삭제되지 않은 댓글 수 */
    long countByUserIdAndIsDeletedFalse(Long userId);
}