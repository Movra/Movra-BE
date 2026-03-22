package com.example.movra.bc.visioning.future_vision.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class FutureVisionCreationFailedException extends CustomException {

    public FutureVisionCreationFailedException() {
        super(ErrorCode.FUTURE_VISION_CREATION_FAILED);
    }
}
