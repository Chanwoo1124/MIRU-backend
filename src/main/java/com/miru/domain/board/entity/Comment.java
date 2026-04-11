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
 * 댓글 엔티티
 *
 * <p>게시글의 댓글과 대댓글을 관리한다. 계층 구조는 1depth로 제한된다:
 * <ul>
 *   <li>최상위 댓글: parent = null</li>
 *   <li>대댓글: parent = 상위 댓글 (대댓글에 댓글을 달 수 없음)</li>
 * </ul>
 * 삭제 시 실제 레코드를 제거하지 않고 내용을 "삭제된 메시지입니다."로 변경하며
 * isDeleted 플래그를 true 로 설정한다 (소프트 삭제).
 * 이렇게 처리하는 이유: 대댓글이 존재하는 댓글이 삭제되어도 스레드 구조를 유지하기 위함.
 */
@Entity
@Getter
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 스펙 준수
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 댓글이 속한 게시글 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    /** 댓글 작성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 댓글 내용 (삭제 시 "삭제된 메시지입니다."로 변경됨, 최대 500자) */
    @Column(nullable = false, length = 500)
    private String content;

    /**
     * 부모 댓글 (최상위 댓글이면 null)
     * 대댓글 여부 판단에도 활용됨 (parent != null → 대댓글)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    /**
     * 대댓글 목록 (작성순 정렬)
     * 최상위 댓글에서만 조회되며, 대댓글에 replies는 항상 빈 리스트임
     */
    @OneToMany(mappedBy = "parent")
    @OrderBy("createdAt ASC")
    private List<Comment> replies = new ArrayList<>();

    /**
     * 소프트 삭제 여부
     * true이면 내용이 이미 "삭제된 메시지입니다."로 변경된 상태
     * 수정 시 isDeleted == true 이면 DELETED_COMMENT_MODIFY 예외 발생
     */
    @Column(nullable = false)
    private boolean isDeleted;

    /** 댓글 수정 일시 */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 댓글 생성 빌더
     *
     * @param context 댓글 내용 (필드명이 content이지만 파라미터는 context로 받아 빌더에서 매핑)
     * @param parent  대댓글인 경우 부모 댓글, 일반 댓글이면 null
     */
    @Builder
    public Comment(Board board, User user, String context, Comment parent) {
        this.board = board;
        this.user = user;
        this.content = context;
        this.parent = parent;
        this.isDeleted = false; // 생성 시 항상 미삭제 상태
    }

    /**
     * 댓글 내용 수정
     *
     * @param content 변경할 내용
     */
    public void updateContent(String content) {
        this.content = content;
    }

    /**
     * 댓글 소프트 삭제
     * 내용을 고정 문구로 변경하고 isDeleted 플래그를 true로 설정한다.
     * 실제 DB 레코드는 유지되어 대댓글 스레드가 깨지지 않음
     */
    public void delete() {
        this.content = "삭제된 메시지입니다.";
        this.isDeleted = true;
    }
}
