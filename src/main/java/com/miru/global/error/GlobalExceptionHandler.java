package com.miru.global.error;

import com.miru.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 전역 예외 처리 핸들러
 *
 * <p>컨트롤러에서 발생하는 예외를 일괄 처리하여 {@link ApiResponse} 형식으로 반환한다.
 * 처리 순서는 구체적인 예외 타입 우선이며, 최후 수단으로 {@link Exception}을 처리한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외 처리
     * 서비스 레이어에서 {@code throw new BusinessException(ErrorType.XXX)} 형태로 발생
     *
     * @return ErrorType에 정의된 HTTP 상태코드와 메시지 반환
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorType errorType = e.getErrorType();
        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.error(errorType.getMessage()));
    }

    /**
     * 요청 값 유효성 검증 실패 (@Valid 어노테이션으로 검증 실패 시)
     * 빈값, 길이 초과, 형식 오류 등 첫 번째 에러 메시지를 반환
     *
     * @return 400 Bad Request + 첫 번째 필드 에러 메시지
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("입력값을 확인해주세요.");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    /**
     * PathVariable 타입 변환 실패
     * 예: GET /api/boards/undefined → Long 파싱 실패
     *
     * @return 400 Bad Request + 어떤 파라미터가 문제인지 명시
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("PathVariable 타입 오류: {} = {}", e.getName(), e.getValue());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("잘못된 요청입니다: " + e.getName() + " 값이 올바르지 않습니다."));
    }

    /**
     * 존재하지 않는 경로 접근 (Spring MVC 404)
     * 정적 리소스나 미등록 API 경로 접근 시 발생
     *
     * @return 404 Not Found
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("요청한 경로를 찾을 수 없습니다."));
    }

    /**
     * 예상치 못한 서버 내부 오류 처리 (최후 수단)
     * 위의 핸들러에서 처리되지 않은 모든 예외를 받아 500으로 반환하고 로그를 남김
     *
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("서버 내부 오류: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorType.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
