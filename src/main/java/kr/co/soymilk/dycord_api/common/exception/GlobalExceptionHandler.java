package kr.co.soymilk.dycord_api.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.soymilk.dycord_api.common.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

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
