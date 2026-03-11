package teamssavice.ssavice.global.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<ProblemDetail>> constraintViolationException(
            ConstraintViolationException e
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        Map<String, Object> errors = new HashMap<>();
        e.getConstraintViolations()
                .forEach(violation ->
                        errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

        problemDetail.setTitle("Validation Error");
        problemDetail.setProperty("errors", errors);
        return Mono.just(ResponseEntity.badRequest().body(problemDetail));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ProblemDetail>> webExchangeBindException(
            WebExchangeBindException e
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation Error");

        Map<String, Object> errors = new HashMap<>();
        e.getFieldErrors().forEach(field ->
                errors.put(field.getField(), field.getDefaultMessage())
        );
        problemDetail.setProperty("errors", errors);

        return Mono.just(ResponseEntity.badRequest().body(problemDetail));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Mono<ResponseEntity<ProblemDetail>> methodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Invalid Parameter Type");
        problemDetail.setDetail(String.format("Parameter '%s' has invalid value '%s'", e.getName(), e.getValue()));
        return Mono.just(ResponseEntity.badRequest().body(problemDetail));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleServerWebInputException(
            ServerWebInputException e
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problemDetail.setTitle("Invalid Request");
        problemDetail.setDetail(resolveDetailMessage(e));
        return Mono.just(ResponseEntity.badRequest().body(problemDetail));
    }

    @ExceptionHandler(CustomException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleCustom(
            CustomException e
    ) {
        ProblemDetail problemDetail = setCustomProblemDetail(e);
        return Mono.just(ResponseEntity
                .status(problemDetail.getStatus())
                .body(problemDetail)
        );
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ProblemDetail>> unexpectedException(Exception e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail("Unknown error");
        log.error("Internal Server Error: {}", e.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problemDetail)
        );
    }

    private ProblemDetail setCustomProblemDetail(CustomException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(e.getErrorCode().getStatus());
        problemDetail.setTitle(e.getTitle());
        problemDetail.setDetail(e.getMessage());
        problemDetail.setProperty("error_code", e.getErrorCode().getCode());
        return problemDetail;
    }

    private String resolveDetailMessage(ServerWebInputException e) {

        // JSON 파싱 오류
        if (e.getCause() instanceof DecodingException) {
            return "요청 바디(JSON) 형식이 올바르지 않습니다.";
        }

        // 타입 변환 실패 등
        if (e.getReason() != null) {
            return e.getReason();
        }

        return "잘못된 요청입니다.";
    }
}
