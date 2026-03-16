package com.miru.domain.alarm.repository;

import com.miru.domain.alarm.entity.Alarm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    /** 특정 유저의 읽지 않은 알람 목록 조회 (최신순, sender fetch join으로 N+1 방지) */
    @Query(value = "SELECT a FROM Alarm a JOIN FETCH a.sender WHERE a.receiveUser.id = :userId AND a.isRead = false ORDER BY a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM Alarm a WHERE a.receiveUser.id = :userId AND a.isRead = false")
    Page<Alarm> findByReceiveUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /** 알람 단건 조회 (본인 알람인지 확인용) */
    java.util.Optional<Alarm> findByIdAndReceiveUserId(Long alarmId, Long userId);

    /** 읽지 않은 알람 존재 여부 */
    boolean existsByReceiveUserIdAndIsReadFalse(Long userId);

    /** 전체 읽음 처리 (벌크 UPDATE) */
    @Modifying
    @Query("UPDATE Alarm a SET a.isRead = true WHERE a.receiveUser.id = :userId AND a.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);
}
