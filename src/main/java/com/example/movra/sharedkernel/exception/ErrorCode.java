package com.example.movra.sharedkernel.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

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
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "잘못된 파일 확장자입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),

    DAILY_PLAN_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 일일 계획입니다."),
    DAILY_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "일일 계획을 찾을 수 없습니다."),
    FUTURE_VISION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 미래 비전입니다."),
    FUTURE_VISION_NOT_FOUND(HttpStatus.NOT_FOUND, "미래 비전을 찾을 수 없습니다."),
    FUTURE_VISION_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "미래 비전 생성에 실패했습니다."),
    FUTURE_VISION_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "미래 비전 수정에 실패했습니다."),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "작업을 찾을 수 없습니다."),
    INVALID_TASK_TYPE(HttpStatus.BAD_REQUEST, "이 작업에 사용할 수 없는 작업 유형입니다."),
    TASK_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "완료된 작업은 수정할 수 없습니다."),
    CORE_SELECTED_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "상위 선택 개수 제한을 초과했습니다."),

    TIMETABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "타임테이블을 찾을 수 없습니다."),
    SLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "슬롯을 찾을 수 없습니다."),
    TIME_OVERLAP(HttpStatus.BAD_REQUEST, "다른 시간과 겹칩니다."),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "유효하지 않은 시간 범위입니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "유효하지 않은 날짜 범위입니다."),
    TOP_PICKS_NOT_FULLY_ASSIGNED(HttpStatus.BAD_REQUEST, "모든 상위 선택 작업을 먼저 배정해야 합니다."),
    TOP_PICK_SLOT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "상위 선택 슬롯 제한을 초과했습니다."),
    NOT_TOP_PICKED_TASK(HttpStatus.BAD_REQUEST, "상위 선택된 작업만 예상 시간을 수정할 수 있습니다."),
    INVALID_TOP_PICK_ESTIMATED_MINUTES(HttpStatus.BAD_REQUEST, "상위 선택 예상 시간은 0보다 커야 합니다."),
    INVALID_TOP_PICK_MEMO(HttpStatus.BAD_REQUEST, "상위 선택 메모는 비어 있을 수 없으며 255자 이하여야 합니다."),
    TOP_PICK_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "상위 선택 상세 정보를 찾을 수 없습니다."),

    TINY_WIN_NOT_FOUND(HttpStatus.NOT_FOUND, "작은 성과를 찾을 수 없습니다."),
    DAILY_REFLECTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 일일 회고입니다."),
    DAILY_REFLECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "일일 회고를 찾을 수 없습니다."),
    INVALID_DAILY_REFLECTION(HttpStatus.BAD_REQUEST, "일일 회고 내용이 유효하지 않습니다."),

    ALREADY_JOINED(HttpStatus.CONFLICT, "이미 해당 방에 참여 중입니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "참여자를 찾을 수 없습니다."),
    NOT_LEADER(HttpStatus.FORBIDDEN, "리더만 이 작업을 수행할 수 있습니다."),
    LEADER_CANNOT_KICK_SELF(HttpStatus.BAD_REQUEST, "리더는 자신을 내보낼 수 없습니다."),
    PRIVATE_ROOM_REQUIRES_INVITE_CODE(HttpStatus.BAD_REQUEST, "비공개 방은 초대 코드가 필요합니다."),
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 초대 코드입니다."),

    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "방을 찾을 수 없습니다."),

    ALREADY_FOCUSING(HttpStatus.BAD_REQUEST, "이미 집중 중입니다."),
    NOT_FOCUSING(HttpStatus.BAD_REQUEST, "현재 집중 중이 아닙니다."),
    FOCUS_SESSION_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "이미 진행 중인 집중 세션이 있습니다."),
    FOCUS_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "집중 세션을 찾을 수 없습니다."),
    FOCUS_SESSION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 집중 세션입니다."),
    INVALID_FOCUS_SESSION(HttpStatus.BAD_REQUEST, "유효하지 않은 집중 세션입니다."),

    DEVICE_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "디바이스 토큰을 찾을 수 없습니다."),

    INVITE_CODE_NOT_GENERATED(HttpStatus.BAD_REQUEST, "초대 코드가 생성되지 않았습니다."),
    INVITE_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "초대 코드가 만료되었습니다."),

    ACCOUNTABILITY_RELATION_NOT_FOUND(HttpStatus.NOT_FOUND, "감시 관계를 찾을 수 없습니다."),
    MONITORING_TARGET_NOT_ALLOWED(HttpStatus.FORBIDDEN, "허용되지 않은 모니터링 대상입니다."),
    NOT_SUBJECT_USER(HttpStatus.FORBIDDEN, "해당 관계의 주체 유저가 아닙니다."),
    WATCHER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 감시자가 존재합니다."),

    CANNOT_JOIN_OWN_ACCOUNTABILITY_RELATION(HttpStatus.BAD_REQUEST, "자신의 감시 관계에는 참여할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
