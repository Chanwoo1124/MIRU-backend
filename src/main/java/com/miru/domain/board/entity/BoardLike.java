package com.miru.domain.board.entity;

import com.miru.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "board_likes", uniqueConstraints = {
        // ✨ 핵심: 유저ID + 게시글ID 조합은 유일해야 함 (중복 좋아요 방지)
        @UniqueConstraint(name = "uk_board_like_user_board", columnNames = {"user_id", "board_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Builder
    public BoardLike(User user, Board board) {
        this.user = user;
        this.board = board;
    }
}