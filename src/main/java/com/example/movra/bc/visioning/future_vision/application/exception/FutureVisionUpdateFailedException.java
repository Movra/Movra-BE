package com.example.movra.bc.visioning.future_vision.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class FutureVisionUpdateFailedException extends CustomException {

    public FutureVisionUpdateFailedException() {
        super(ErrorCode.FUTURE_VISION_UPDATE_FAILED);
    }
}
