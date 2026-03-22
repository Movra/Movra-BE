package com.example.movra.bc.visioning.future_vision.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class FutureVisionAlreadyExistsException extends CustomException {

    public FutureVisionAlreadyExistsException() {
        super(ErrorCode.FUTURE_VISION_ALREADY_EXISTS);
    }
}
