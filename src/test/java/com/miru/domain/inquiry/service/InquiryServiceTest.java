package com.miru.domain.inquiry.service;

import com.miru.domain.inquiry.dto.InquiryCreateRequestDto;
import com.miru.domain.inquiry.dto.InquiryDetailResponseDto;
import com.miru.domain.inquiry.dto.InquiryListResponseDto;
import com.miru.domain.inquiry.entity.InquiryBoard;
import com.miru.domain.inquiry.repository.InquiryRepository;
import com.miru.domain.user.entity.Role;
import com.miru.domain.user.entity.User;
import com.miru.domain.user.repository.UserRepository;
import com.miru.global.auth.dto.SessionUser;
import com.miru.global.error.BusinessException;
import com.miru.global.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * InquiryService 단위 테스트
 * Repository는 Mockito로 mock하여 서비스 비즈니스 로직만 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InquiryService inquiryService;

    private User user;
    private User otherUser;
    private SessionUser sessionUser;
    private SessionUser otherSessionUser;
    private InquiryBoard inquiry;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 생성
        user = User.builder()
                .email("user@test.com")
                .nickname("유저A")
                .loginFrom("google")
                .loginFromId("google-user-1")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.activate();

        otherUser = User.builder()
                .email("other@test.com")
                .nickname("유저B")
                .loginFrom("google")
                .loginFromId("google-user-2")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(otherUser, "id", 2L);
        otherUser.activate();

        sessionUser = new SessionUser(user);
        otherSessionUser = new SessionUser(otherUser);

        // 테스트용 문의 생성
        inquiry = InquiryBoard.builder()
                .user(user)
                .title("서비스 이용 문의")
                .content("어떻게 사용하나요?")
                .build();
        ReflectionTestUtils.setField(inquiry, "id", 1L);
    }

    // ===================== getInquiries =====================

    @Nested
    @DisplayName("getInquiries - 내 문의 목록 조회")
    class GetInquiriesTest {

        @Test
        @DisplayName("내 문의 목록 조회 성공")
        void getInquiries_성공() {
            // given
            given(inquiryRepository.findAllByUserIdOrderByCreatedAtDesc(1L))
                    .willReturn(List.of(inquiry));

            // when
            InquiryListResponseDto result = inquiryService.getInquiries(sessionUser);

            // then
            assertThat(result.getTotalCount()).isEqualTo(1);
            assertThat(result.getItems().get(0).getTitle()).isEqualTo("서비스 이용 문의");
        }

        @Test
        @DisplayName("비로그인 유저: UNAUTHORIZED 예외 발생")
        void getInquiries_비로그인() {
            assertThatThrownBy(() -> inquiryService.getInquiries(null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.UNAUTHORIZED);
        }
    }

    // ===================== getInquiry =====================

    @Nested
    @DisplayName("getInquiry - 문의 상세 조회")
    class GetInquiryTest {

        @Test
        @DisplayName("본인 문의 상세 조회 성공")
        void getInquiry_성공() {
            // given
            given(inquiryRepository.findByIdWithAnswerAndAdmin(1L)).willReturn(Optional.of(inquiry));

            // when
            InquiryDetailResponseDto result = inquiryService.getInquiry(1L, sessionUser);

            // then
            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getItems().get(0).getTitle()).isEqualTo("서비스 이용 문의");
        }

        @Test
        @DisplayName("타인의 문의 조회: FORBIDDEN 예외 발생")
        void getInquiry_권한없음() {
            // given - otherUser의 문의를 user가 조회 시도
            given(inquiryRepository.findByIdWithAnswerAndAdmin(1L)).willReturn(Optional.of(inquiry));

            // when & then - sessionUser(user)가 아닌 otherSessionUser로 조회
            assertThatThrownBy(() -> inquiryService.getInquiry(1L, otherSessionUser))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.FORBIDDEN);
        }

        @Test
        @DisplayName("존재하지 않는 문의: INQUIRY_NOT_FOUND 예외 발생")
        void getInquiry_없는문의() {
            // given
            given(inquiryRepository.findByIdWithAnswerAndAdmin(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inquiryService.getInquiry(999L, sessionUser))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.INQUIRY_NOT_FOUND);
        }
    }

    // ===================== createInquiry =====================

    @Nested
    @DisplayName("createInquiry - 문의 작성")
    class CreateInquiryTest {

        private InquiryCreateRequestDto dto;

        @BeforeEach
        void setUp() {
            // @AllArgsConstructor 활용
            dto = new InquiryCreateRequestDto("새 문의입니다", "자세한 내용입니다.");
        }

        @Test
        @DisplayName("문의 작성 성공: 저장 후 상세 정보 반환")
        void createInquiry_성공() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(inquiryRepository.save(any(InquiryBoard.class))).willAnswer(invocation -> {
                InquiryBoard saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 2L);
                return saved;
            });

            // when
            InquiryDetailResponseDto result = inquiryService.createInquiry(sessionUser, dto);

            // then
            verify(inquiryRepository, times(1)).save(any(InquiryBoard.class));
            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getItems().get(0).getTitle()).isEqualTo("새 문의입니다");
        }

        @Test
        @DisplayName("비로그인 유저: UNAUTHORIZED 예외 발생")
        void createInquiry_비로그인() {
            assertThatThrownBy(() -> inquiryService.createInquiry(null, dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.UNAUTHORIZED);
        }
    }

    // ===================== deleteInquiry =====================

    @Nested
    @DisplayName("deleteInquiry - 문의 삭제")
    class DeleteInquiryTest {

        @Test
        @DisplayName("본인 문의 삭제 성공")
        void deleteInquiry_성공() {
            // given
            given(inquiryRepository.findById(1L)).willReturn(Optional.of(inquiry));

            // when
            inquiryService.deleteInquiry(1L, sessionUser);

            // then
            verify(inquiryRepository, times(1)).delete(inquiry);
        }

        @Test
        @DisplayName("타인의 문의 삭제 시도: FORBIDDEN 예외 발생")
        void deleteInquiry_권한없음() {
            // given - user가 작성한 문의를 otherUser가 삭제 시도
            given(inquiryRepository.findById(1L)).willReturn(Optional.of(inquiry));

            // when & then
            assertThatThrownBy(() -> inquiryService.deleteInquiry(1L, otherSessionUser))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType").isEqualTo(ErrorType.FORBIDDEN);

            // 삭제가 호출되지 않아야 함
            verify(inquiryRepository, never()).delete(any(InquiryBoard.class));
        }
    }
}
