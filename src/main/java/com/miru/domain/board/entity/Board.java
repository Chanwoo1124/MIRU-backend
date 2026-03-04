package com.miru.domain.board.entity;

import com.miru.domain.user.entity.User;
import com.miru.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "boards")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "integer default 0", nullable = false)
    private int viewCount;

    @Column(columnDefinition = "integer default 0", nullable = false)
    private int commentCount;

    @Column(columnDefinition = "integer default 0", nullable = false)
    private int likeCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardType type;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<BoardLike> likes = new ArrayList<>();


    @Builder
    public Board(User user, String title, String content, BoardType type) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.type = type;
    }

    /** 게시글 수정 */
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /** 조회수 증가 */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /** 댓글수 증가 */
    public void incrementCommentCount() {
        this.commentCount++;
    }

    /** 좋아요수 증가 */
    public void incrementLikeCount() {
        this.likeCount++;
    }

    /** 좋아요수 감소 */
    public void decrementLikeCount() {
        this.likeCount--;
    }
}
