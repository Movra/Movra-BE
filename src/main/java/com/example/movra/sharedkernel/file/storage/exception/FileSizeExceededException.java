package com.example.movra.sharedkernel.file.storage.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class FileSizeExceededException extends CustomException {

    public FileSizeExceededException() {
        super(ErrorCode.FILE_SIZE_EXCEEDED);
    }
}
