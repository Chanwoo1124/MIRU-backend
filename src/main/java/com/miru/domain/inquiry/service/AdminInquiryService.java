package com.miru.domain.inquiry.service;

import com.miru.domain.alarm.entity.AlarmType;
import com.miru.domain.alarm.service.AlarmService;
import com.miru.domain.inquiry.dto.AdminInquiryAnswerRequestDto;
import com.miru.domain.inquiry.dto.AdminInquiryDetailResponseDto;
import com.miru.domain.inquiry.dto.AdminInquiryListResponseDto;
import com.miru.domain.inquiry.entity.InquiryAnswer;
import com.miru.domain.inquiry.entity.InquiryBoard;
import com.miru.domain.inquiry.repository.InquiryAnswerRepository;
import com.miru.domain.inquiry.repository.InquiryRepository;
import com.miru.domain.user.entity.User;
import com.miru.domain.user.repository.UserRepository;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.error.BusinessException;
import com.miru.global.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 문의 게시판 관리자 서비스
 *
 * <p>관리자가 전체 문의를 조회하고 답변을 등록/삭제하는 기능을 제공한다.
 * 답변 등록 시:
 * <ol>
 *   <li>InquiryAnswer 저장</li>
 *   <li>InquiryBoard 상태 WAITING → COMPLETED 변경</li>
 *   <li>문의 작성자에게 INQUIRY 타입 알람 발송</li>
 * </ol>
 * 답변 삭제 시:
 * <ol>
 *   <li>InquiryBoard.clearAnswer() → orphanRemoval로 InquiryAnswer 자동 삭제</li>
 *   <li>InquiryBoard 상태 COMPLETED → WAITING 복구</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final UserRepository userRepository;
    private final AlarmService alarmService;

    /** 전체 문의 목록 조회 */
    public AdminInquiryListResponseDto getInquiries() {
        List<InquiryBoard> inquiries = inquiryRepository.findAllWithUserOrderByCreatedAtDesc();

        List<AdminInquiryListResponseDto.Item> items = inquiries.stream()
                .map(i -> new AdminInquiryListResponseDto.Item(
                        i.getId(),
                        i.getStatus(),
                        i.getUser().getNickname(),
                        i.getTitle(),
                        i.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new AdminInquiryListResponseDto(items.size(), items);
    }

    /** 문의 상세 조회 */
    public AdminInquiryDetailResponseDto getInquiry(Long id) {
        InquiryBoard inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorType.INQUIRY_NOT_FOUND));

        String answerContent = inquiry.getAnswer() != null ? inquiry.getAnswer().getContent() : null;

        AdminInquiryDetailResponseDto.Item item = new AdminInquiryDetailResponseDto.Item(
                inquiry.getId(),
                inquiry.getTitle(),
                inquiry.getUser().getNickname(),
                inquiry.getCreatedAt(),
                inquiry.getStatus(),
                inquiry.getContent(),
                answerContent
        );

        return new AdminInquiryDetailResponseDto(List.of(item));
    }

    /** 관리자 답변 삭제 - 상태 WAITING으로 복구 */
    @Transactional
    public void deleteAnswer(Long id) {
        InquiryBoard inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorType.INQUIRY_NOT_FOUND));

        if (inquiry.getAnswer() == null) {
            throw new BusinessException(ErrorType.INQUIRY_ANSWER_NOT_FOUND);
        }

        // answer를 null로 설정하면 orphanRemoval에 의해 자동 삭제됨
        inquiry.clearAnswer();
        inquiry.reopen();
    }

    /** 관리자 답변 등록 */
    @Transactional
    public void createAnswer(Long id, SessionUser sessionUser, AdminInquiryAnswerRequestDto dto) {
        InquiryBoard inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorType.INQUIRY_NOT_FOUND));

        // 이미 답변이 존재하는 경우
        if (inquiry.getAnswer() != null) {
            throw new BusinessException(ErrorType.INQUIRY_ALREADY_ANSWERED);
        }

        User admin = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorType.USER_NOT_FOUND));

        InquiryAnswer answer = InquiryAnswer.builder()
                .inquiryBoard(inquiry)
                .admin(admin)
                .content(dto.getAnswerContent())
                .build();
        inquiryAnswerRepository.save(answer);

        // 문의 상태 WAITING → COMPLETED 변경
        inquiry.complete();

        // 문의 작성자에게 알람
        alarmService.createAlarm(
                inquiry.getUser(), admin, AlarmType.INQUIRY,
                "문의하신 내용에 답변이 등록되었습니다.", "/inquiries/" + id);
    }
}
