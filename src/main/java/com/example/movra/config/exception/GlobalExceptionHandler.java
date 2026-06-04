package com.example.movra.config.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;
import com.example.movra.sharedkernel.exception.ErrorResponse;
import com.example.movra.sharedkernel.exception.ValidationErrorMessageResolver;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> customExceptionHandling(CustomException e){
        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode());
        return ResponseEntity.status(errorResponse.httpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValidExceptionHandling(MethodArgumentNotValidException e) {
        String message = ValidationErrorMessageResolver.resolve(e.getBindingResult());
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_REQUEST, message);
        return ResponseEntity.status(errorResponse.httpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> bindExceptionHandling(BindException e) {
        String message = ValidationErrorMessageResolver.resolve(e.getBindingResult());
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_REQUEST, message);
        return ResponseEntity.status(errorResponse.httpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> invalidRequestExceptionHandling(Exception e) {
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_REQUEST);
        return ResponseEntity.status(errorResponse.httpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> maxUploadSizeExceededHandling(MaxUploadSizeExceededException e) {
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.FILE_SIZE_EXCEEDED);
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
