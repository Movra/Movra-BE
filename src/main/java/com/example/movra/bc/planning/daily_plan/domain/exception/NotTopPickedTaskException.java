package com.example.movra.bc.planning.daily_plan.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class NotTopPickedTaskException extends CustomException {

    public NotTopPickedTaskException() {
        super(ErrorCode.NOT_TOP_PICKED_TASK);
    }
}
