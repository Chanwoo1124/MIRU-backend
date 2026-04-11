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

/**
 * 게시글 엔티티
 *
 * <p>게시판의 게시글을 관리한다. 게시글 타입(type)에 따라 목록 정렬 순서가 결정된다:
 * <ul>
 *   <li>NOTICE  - 공지글: 항상 목록 최상단에 고정 (관리자만 작성 가능)</li>
 *   <li>GENERAL - 일반글: 최신순 정렬</li>
 * </ul>
 * 조회수/좋아요수/댓글수는 DB 집계 쿼리 없이 카운터 필드로 관리한다 (성능 최적화).
 */
@Entity
@Getter
@Table(name = "boards")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 스펙 준수 - 외부 직접 생성 방지
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 게시글 작성자 (탈퇴 시 "탈퇴한 사용자"로 표시, 실제 연결은 유지) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 게시글 제목 (최대 50자) */
    @Column(nullable = false, length = 50)
    private String title;

    /** 게시글 본문 (길이 제한 없음) */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 조회수 (getBoard 호출 시 자동 증가) */
    @Column(columnDefinition = "integer default 0", nullable = false)
    private int viewCount;

    /** 댓글 수 (댓글 작성/삭제 시 카운터 업데이트) */
    @Column(columnDefinition = "integer default 0", nullable = false)
    private int commentCount;

    /** 좋아요 수 (좋아요 토글 시 카운터 업데이트) */
    @Column(columnDefinition = "integer default 0", nullable = false)
    private int likeCount;

    /** 게시글 타입 (NOTICE: 공지글 / GENERAL: 일반글) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardType type;

    /** 게시글 수정 일시 */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 게시글에 속한 댓글 목록
     * 게시글 삭제 시 cascade + orphanRemoval 로 댓글도 함께 삭제됨
     */
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    /**
     * 게시글에 달린 좋아요 목록
     * 게시글 삭제 시 cascade + orphanRemoval 로 좋아요 레코드도 함께 삭제됨
     */
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<BoardLike> likes = new ArrayList<>();


    /**
     * 게시글 생성 빌더
     * type은 서비스 레이어에서 작성자의 role을 확인하여 주입함 (ADMIN → NOTICE, USER → GENERAL)
     */
    @Builder
    public Board(User user, String title, String content, BoardType type) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.type = type;
    }

    /**
     * 게시글 제목/내용 수정
     *
     * @param title   변경할 제목
     * @param content 변경할 내용
     */
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * 조회수 1 증가
     * getBoard() 호출 시마다 실행됨 (자신이 조회해도 증가)
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 댓글 수 1 증가
     * 댓글 작성 시 호출 (삭제는 delete() 처리이므로 감소 없음)
     */
    public void incrementCommentCount() {
        this.commentCount++;
    }

    /**
     * 좋아요 수 1 증가
     * toggleLike() - 좋아요 추가 시 호출
     */
    public void incrementLikeCount() {
        this.likeCount++;
    }

    /**
     * 좋아요 수 1 감소
     * toggleLike() - 좋아요 취소 시 호출
     */
    public void decrementLikeCount() {
        this.likeCount--;
    }
}
