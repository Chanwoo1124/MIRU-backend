package com.miru.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모든 JPA 엔티티가 공통으로 상속받는 기반 클래스
 *
 * <p>Spring Data JPA Auditing을 이용해 생성 시각을 자동으로 기록한다.
 * {@code @EnableJpaAuditing}은 MiruBackendApplication에 선언되어 있다.
 *
 * <p>적용 방법:
 * <pre>
 *   public class MyEntity extends BaseEntity { ... }
 * </pre>
 */
@Getter
@MappedSuperclass // 실제 테이블을 생성하지 않고 자식 엔티티의 컬럼으로 매핑됨
@EntityListeners(AuditingEntityListener.class) // Auditing 기능 활성화
public abstract class BaseEntity {

    /**
     * 레코드 생성 일시 (자동 삽입, 수정 불가)
     * JPA Auditing이 INSERT 시점에 자동으로 값을 채워준다.
     */
    @CreatedDate
    @Column(name = "create_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
