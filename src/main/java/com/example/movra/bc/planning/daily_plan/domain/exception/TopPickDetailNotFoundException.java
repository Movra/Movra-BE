package com.example.movra.bc.planning.daily_plan.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class TopPickDetailNotFoundException extends CustomException {

    public TopPickDetailNotFoundException() {
        super(ErrorCode.TOP_PICK_DETAIL_NOT_FOUND);
    }
}
