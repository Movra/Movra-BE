package com.example.movra.sharedkernel.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에러 입니다."),

    EXPIRED_JWT(HttpStatus.UNAUTHORIZED, "로그인 세션이 만료되었습니다."),
    INVALID_JWT(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    DUPLICATE_ACCOUNT_ID(HttpStatus.CONFLICT, "이미 존재하는 accountId 입니다."),
    DUPLICATE_USER(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "accountId가 존재하지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "비밀번호가 다릅니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "유저 생성에 실패했습니다."),
    PENDING_OAUTH_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 Oauth 사용자입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "리프레시 토큰을 찾을 수 없습니다."),

    IMAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "이미지가 존재하지 않습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "허용되지 않은 파일 확장자입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),

    DAILY_PLAN_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 날짜에 이미 일일 계획이 존재합니다."),
    DAILY_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 날짜의 일일 계획이 존재하지 않습니다."),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 할 일이 존재하지 않습니다."),
    TASK_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "완료된 할 일은 수정할 수 없습니다."),
    CORE_SELECTED_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "최우선 할 일은 최대 3개까지만 지정할 수 있습니다."),

    TIMETABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "타임테이블이 존재하지 않습니다."),
    SLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "슬롯이 존재하지 않습니다."),
    TIME_OVERLAP(HttpStatus.BAD_REQUEST, "다른 슬롯과 시간이 겹칩니다."),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "시작 시간이 종료 시간보다 늦을 수 없습니다."),
    TOP_PICKS_NOT_FULLY_ASSIGNED(HttpStatus.BAD_REQUEST, "최우선 할 일을 모두 배치한 후에 다른 항목을 추가할 수 있습니다."),
    TOP_PICK_SLOT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "최우선 할 일 슬롯이 이미 모두 배정되었습니다."),
    NOT_TOP_PICKED_TASK(HttpStatus.BAD_REQUEST, "최우선 할 일만 예상 시간을 변경할 수 있습니다."),
    TOP_PICK_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "TopPickDetail이 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
