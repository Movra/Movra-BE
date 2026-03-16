package com.example.morva.sharedkernel.file.storage.exception;

import com.example.morva.sharedkernel.exception.CustomException;
import com.example.morva.sharedkernel.exception.ErrorCode;

public class FileDeleteFailedException extends CustomException {

    public FileDeleteFailedException() {
        super(ErrorCode.FILE_DELETE_FAILED);
    }
}
