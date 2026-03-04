package com.miru.domain.board.repository;

import com.miru.domain.board.entity.BoardLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {

    /** 좋아요 여부 확인 */
    boolean existsByUserIdAndBoardId(Long userId, Long boardId);

    /** 좋아요 취소 */
    void deleteByUserIdAndBoardId(Long userId, Long boardId);
}
