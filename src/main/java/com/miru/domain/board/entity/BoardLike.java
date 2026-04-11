package com.miru.domain.board.entity;

import com.miru.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글 좋아요 엔티티
 *
 * <p>유저와 게시글의 다대다 관계를 중간 테이블로 관리한다.
 * 유일 제약(uk_board_like_user_board)으로 중복 좋아요를 DB 레벨에서 방지한다.
 * 좋아요 토글 시:
 * <ol>
 *   <li>레코드 존재 → 삭제 + likeCount 감소</li>
 *   <li>레코드 없음 → 저장 + likeCount 증가</li>
 * </ol>
 */
@Entity
@Getter
@Table(name = "board_likes", uniqueConstraints = {
        // 유저ID + 게시글ID 조합은 유일해야 함 (중복 좋아요 방지)
        @UniqueConstraint(name = "uk_board_like_user_board", columnNames = {"user_id", "board_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 좋아요를 누른 유저 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 좋아요가 달린 게시글 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Builder
    public BoardLike(User user, Board board) {
        this.user = user;
        this.board = board;
    }
}
