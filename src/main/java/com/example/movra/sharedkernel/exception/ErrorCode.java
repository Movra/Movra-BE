package com.example.movra.sharedkernel.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    EXPIRED_JWT(HttpStatus.UNAUTHORIZED, "JWT 토큰이 만료되었습니다."),
    INVALID_JWT(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다."),

    DUPLICATE_ACCOUNT_ID(HttpStatus.CONFLICT, "이미 존재하는 계정 ID 입니다."),
    DUPLICATE_USER(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "로그인에 실패했습니다."),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "계정 ID를 찾을 수 없습니다."),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 생성에 실패했습니다."),
    PENDING_OAUTH_NOT_FOUND(HttpStatus.NOT_FOUND, "대기 중인 OAuth 사용자를 찾을 수 없습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "리프레시 토큰을 찾을 수 없습니다."),

    IMAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "이미지를 찾을 수 없습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 확장자입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),

    DAILY_PLAN_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 날짜의 데일리 플랜이 이미 존재합니다."),
    DAILY_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "데일리 플랜을 찾을 수 없습니다."),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "작업을 찾을 수 없습니다."),
    INVALID_TASK_TYPE(HttpStatus.BAD_REQUEST, "해당 작업 유형으로는 이 작업을 수행할 수 없습니다."),
    TASK_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "완료된 작업은 수정할 수 없습니다."),
    CORE_SELECTED_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "Top Pick 선택 개수를 초과했습니다."),

    TIMETABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "타임테이블을 찾을 수 없습니다."),
    SLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "슬롯을 찾을 수 없습니다."),
    TIME_OVERLAP(HttpStatus.BAD_REQUEST, "다른 슬롯과 시간이 겹칩니다."),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "유효하지 않은 시간 범위입니다."),
    TOP_PICKS_NOT_FULLY_ASSIGNED(HttpStatus.BAD_REQUEST, "모든 Top Pick이 먼저 할당되어야 합니다."),
    TOP_PICK_SLOT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "Top Pick 슬롯 개수를 초과했습니다."),
    NOT_TOP_PICKED_TASK(HttpStatus.BAD_REQUEST, "Top Pick 으로 선택된 작업만 예상 시간을 수정할 수 있습니다."),
    TOP_PICK_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "Top Pick 상세 정보를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}