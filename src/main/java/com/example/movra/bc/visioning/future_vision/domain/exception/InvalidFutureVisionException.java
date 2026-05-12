package com.example.movra.bc.visioning.future_vision.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidFutureVisionException extends CustomException {

    public InvalidFutureVisionException() {
        super(ErrorCode.INVALID_FUTURE_VISION);
    }
}
