package com.miru.domain.inquiry.service;

import com.miru.domain.inquiry.dto.InquiryCreateRequestDto;
import com.miru.domain.inquiry.dto.InquiryDetailResponseDto;
import com.miru.domain.inquiry.dto.InquiryListResponseDto;
import com.miru.domain.inquiry.entity.InquiryAnswer;
import com.miru.domain.inquiry.entity.InquiryBoard;
import com.miru.domain.inquiry.repository.InquiryRepository;
import com.miru.domain.user.entity.User;
import com.miru.domain.user.repository.UserRepository;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.error.BusinessException;
import com.miru.global.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    /** 내 문의 목록 전체 조회 */
    public InquiryListResponseDto getInquiries(SessionUser sessionUser) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        List<InquiryBoard> inquiries = inquiryRepository.findAllByUserIdOrderByCreatedAtDesc(sessionUser.getId());

        List<InquiryListResponseDto.Item> items = inquiries.stream()
                .map(i -> new InquiryListResponseDto.Item(i.getId(), i.getTitle(), i.getCreatedAt(), i.getStatus()))
                .collect(Collectors.toList());

        return new InquiryListResponseDto(items.size(), items);
    }

    /** 문의 상세 조회 */
    public InquiryDetailResponseDto getInquiry(Long id, SessionUser sessionUser) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        InquiryBoard inquiry = inquiryRepository.findByIdWithAnswerAndAdmin(id)
                .orElseThrow(() -> new BusinessException(ErrorType.INQUIRY_NOT_FOUND));

        if (!inquiry.getUser().getId().equals(sessionUser.getId())) {
            throw new BusinessException(ErrorType.FORBIDDEN);
        }

        return new InquiryDetailResponseDto(List.of(buildDetailItem(inquiry)));
    }

    /** 문의 작성 */
    @Transactional
    public InquiryDetailResponseDto createInquiry(SessionUser sessionUser, InquiryCreateRequestDto dto) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorType.USER_NOT_FOUND));

        InquiryBoard inquiry = InquiryBoard.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();
        inquiryRepository.save(inquiry);

        return new InquiryDetailResponseDto(List.of(buildDetailItem(inquiry)));
    }

    /** 문의 삭제 */
    @Transactional
    public void deleteInquiry(Long id, SessionUser sessionUser) {
        if (sessionUser == null) throw new BusinessException(ErrorType.UNAUTHORIZED);

        InquiryBoard inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorType.INQUIRY_NOT_FOUND));

        if (!inquiry.getUser().getId().equals(sessionUser.getId())) {
            throw new BusinessException(ErrorType.FORBIDDEN);
        }

        inquiryRepository.delete(inquiry);
    }

    /** 상세 응답 Item 빌드 (공통) */
    private InquiryDetailResponseDto.Item buildDetailItem(InquiryBoard inquiry) {
        InquiryAnswer answer = inquiry.getAnswer();
        InquiryDetailResponseDto.AnswerItem answerItem = null;

        if (answer != null) {
            answerItem = new InquiryDetailResponseDto.AnswerItem(
                    answer.getAdmin().getNickname(),
                    answer.getContent(),
                    answer.getCreatedAt()
            );
        }

        return new InquiryDetailResponseDto.Item(
                inquiry.getId(),
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.getCreatedAt(),
                inquiry.getStatus(),
                answerItem
        );
    }
}
