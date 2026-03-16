package com.example.movra.sharedkernel.file.storage.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidFileExtensionException extends CustomException {

    public InvalidFileExtensionException() {
        super(ErrorCode.INVALID_FILE_EXTENSION);
    }
}
