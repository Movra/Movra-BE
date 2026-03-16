package com.example.morva.config.exception;

import com.example.morva.sharedkernel.exception.CustomException;
import com.example.morva.sharedkernel.exception.ErrorCode;
import com.example.morva.sharedkernel.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> customExceptionHandling(CustomException e){
        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode());
        return ResponseEntity.status(errorResponse.httpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exceptionHandling(Exception e) {
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        log.error("예상치 목한 에러 발생 : ", e);
        return ResponseEntity.status(errorResponse.httpStatus())
                .body(errorResponse);
    }
}
