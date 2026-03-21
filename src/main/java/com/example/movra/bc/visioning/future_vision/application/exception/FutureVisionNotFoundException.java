package com.example.movra.bc.visioning.future_vision.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class FutureVisionNotFoundException extends CustomException {

    public FutureVisionNotFoundException() {
        super(ErrorCode.FUTURE_VISION_NOT_FOUND);
    }
}
