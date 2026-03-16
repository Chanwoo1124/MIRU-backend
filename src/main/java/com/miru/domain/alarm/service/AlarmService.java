package com.miru.domain.alarm.service;

import com.miru.domain.alarm.dto.AlarmHasUnreadResponseDto;
import com.miru.domain.alarm.dto.AlarmListResponseDto;
import com.miru.domain.alarm.entity.Alarm;
import com.miru.domain.alarm.entity.AlarmType;
import com.miru.domain.alarm.repository.AlarmRepository;
import com.miru.domain.user.entity.User;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.error.BusinessException;
import com.miru.global.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlarmService {

    private final AlarmRepository alarmRepository;

    /** 알람 목록 조회 (최신순, 10개씩) */
    public AlarmListResponseDto getAlarms(SessionUser sessionUser, int page) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        Page<Alarm> alarmPage = alarmRepository.findByReceiveUserIdOrderByCreatedAtDesc(
                sessionUser.getId(), PageRequest.of(page, 10));

        List<AlarmListResponseDto.Item> items = alarmPage.getContent().stream()
                .map(a -> new AlarmListResponseDto.Item(
                        a.getId(),
                        a.getType(),
                        a.getContent(),
                        a.getSender().getNickname(),
                        a.getTargetUrl(),
                        a.isRead(),
                        a.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new AlarmListResponseDto(alarmPage.getTotalElements(), items);
    }

    /** 읽지 않은 알람 존재 여부 조회 */
    public AlarmHasUnreadResponseDto hasUnread(SessionUser sessionUser) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        boolean hasUnread = alarmRepository.existsByReceiveUserIdAndIsReadFalse(sessionUser.getId());
        return new AlarmHasUnreadResponseDto(hasUnread);
    }

    /** 단건 읽음 처리 */
    @Transactional
    public void readOne(SessionUser sessionUser, Long alarmId) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        Alarm alarm = alarmRepository.findByIdAndReceiveUserId(alarmId, sessionUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorType.UNAUTHORIZED));

        alarm.read();
    }

    /** 전체 읽음 처리 */
    @Transactional
    public void readAll(SessionUser sessionUser) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);
        alarmRepository.markAllAsReadByUserId(sessionUser.getId());
    }

    /**
     * 알람 생성 (BoardService, AdminInquiryService 내부 호출용)
     * 본인에게 발생하는 알람은 저장하지 않음
     */
    @Transactional
    public void createAlarm(User receiveUser, User sender, AlarmType type, String content, String targetUrl) {
        // 본인 알람 제외
        if (receiveUser.getId().equals(sender.getId())) {
            return;
        }

        Alarm alarm = Alarm.builder()
                .receiveUser(receiveUser)
                .sender(sender)
                .type(type)
                .content(content)
                .targetUrl(targetUrl)
                .build();
        alarmRepository.save(alarm);
    }
}
