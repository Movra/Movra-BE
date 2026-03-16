package com.example.morva.sharedkernel.file.storage.exception;

import com.example.morva.sharedkernel.exception.CustomException;
import com.example.morva.sharedkernel.exception.ErrorCode;

public class InvalidFileExtensionException extends CustomException {

    public InvalidFileExtensionException() {
        super(ErrorCode.INVALID_FILE_EXTENSION);
    }
}
