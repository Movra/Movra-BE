package com.example.movra.sharedkernel.file.storage.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class FileDeleteFailedException extends CustomException {

    public FileDeleteFailedException() {
        super(ErrorCode.FILE_DELETE_FAILED);
    }
}
