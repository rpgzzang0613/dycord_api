package kr.co.soymilk.dycord_api.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.soymilk.dycord_api.common.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class})
    public ResponseEntity<ErrorResponseDto> handleHttpStatusException(Exception e, HttpServletRequest request) {
        HttpStatusCode statusCode;
        String msg;

        switch (e) {
            case HttpClientErrorException clientError -> {
                statusCode = clientError.getStatusCode();
                msg = clientError.getMessage();
            }
            case HttpServerErrorException serverError -> {
                statusCode = serverError.getStatusCode();
                msg = serverError.getMessage();
            }
            default -> {
                // 있을 수 없는 상황
                statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
                msg = "알 수 없는 오류 발생";
            }
        }

        String clientIp = request.getHeader("X-FORWARDED-FOR");
        if (clientIp == null) {
            clientIp = request.getRemoteAddr();
        }

        String requestUri = request.getRequestURI();

        log.error("\nClientIP - {} | RequestUrl - {} | StatusCode - {} | message - {}", clientIp, requestUri, statusCode, msg);

        ErrorResponseDto errorResDto = ErrorResponseDto.builder()
                .message("StatusCode: " + statusCode + ", message: " + e.getMessage())
                .build();

        return new ResponseEntity<>(errorResDto, statusCode);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(Exception e, HttpServletRequest request) {
        String clientIp = request.getHeader("X-FORWARDED-FOR");
        if (clientIp == null) {
            clientIp = request.getRemoteAddr();
        }

        String requestUri = request.getRequestURI();

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTraceStr = sw.toString();

        log.error("\nClientIP - {} | RequestUrl - {}\n----- Exception StackTrace -----\n{}", clientIp, requestUri, stackTraceStr);

        ErrorResponseDto errorResDto = ErrorResponseDto.builder()
                .message(e.getMessage())
                .build();

        return new ResponseEntity<>(errorResDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
