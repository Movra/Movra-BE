package com.example.movra.bc.planning.exam_schedule.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidExamScheduleException extends CustomException {

    public InvalidExamScheduleException() {
        super(ErrorCode.INVALID_EXAM_SCHEDULE);
    }
}
