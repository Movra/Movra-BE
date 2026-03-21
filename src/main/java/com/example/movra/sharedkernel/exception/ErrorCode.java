package com.example.movra.sharedkernel.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error."),

    EXPIRED_JWT(HttpStatus.UNAUTHORIZED, "JWT token has expired."),
    INVALID_JWT(HttpStatus.UNAUTHORIZED, "Invalid JWT token."),

    DUPLICATE_ACCOUNT_ID(HttpStatus.CONFLICT, "Account ID already exists."),
    DUPLICATE_USER(HttpStatus.CONFLICT, "User already exists."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "Email already exists."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "Login failed."),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "Account ID was not found."),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "Password does not match."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User was not found."),
    USER_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "User creation failed."),
    PENDING_OAUTH_NOT_FOUND(HttpStatus.NOT_FOUND, "Pending OAuth user was not found."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Refresh token was not found."),

    IMAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "Image was not found."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "Invalid file extension."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "File delete failed."),

    DAILY_PLAN_ALREADY_EXISTS(HttpStatus.CONFLICT, "Daily plan already exists."),
    DAILY_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "Daily plan was not found."),
    FUTURE_VISION_ALREADY_EXISTS(HttpStatus.CONFLICT, "Future vision already exists."),
    FUTURE_VISION_NOT_FOUND(HttpStatus.NOT_FOUND, "Future vision was not found."),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "Task was not found."),
    INVALID_TASK_TYPE(HttpStatus.BAD_REQUEST, "Invalid task type for this operation."),
    TASK_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "Completed task cannot be modified."),
    CORE_SELECTED_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "Top pick selection limit exceeded."),

    TIMETABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "Timetable was not found."),
    SLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "Slot was not found."),
    TIME_OVERLAP(HttpStatus.BAD_REQUEST, "Time overlaps with another slot."),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "Invalid time range."),
    TOP_PICKS_NOT_FULLY_ASSIGNED(HttpStatus.BAD_REQUEST, "All top picks must be assigned first."),
    TOP_PICK_SLOT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "Top pick slot limit exceeded."),
    NOT_TOP_PICKED_TASK(HttpStatus.BAD_REQUEST, "Only top-picked tasks can update estimated time."),
    TOP_PICK_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "Top pick detail was not found.");

    private final HttpStatus httpStatus;
    private final String message;
}
