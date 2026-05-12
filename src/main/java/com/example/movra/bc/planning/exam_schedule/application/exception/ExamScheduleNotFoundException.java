package com.example.movra.bc.planning.exam_schedule.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class ExamScheduleNotFoundException extends CustomException {

    public ExamScheduleNotFoundException() {
        super(ErrorCode.EXAM_SCHEDULE_NOT_FOUND);
    }
}
