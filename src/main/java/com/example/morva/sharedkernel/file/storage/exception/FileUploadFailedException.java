package com.example.morva.sharedkernel.file.storage.exception;

import com.example.morva.sharedkernel.exception.CustomException;
import com.example.morva.sharedkernel.exception.ErrorCode;

public class FileUploadFailedException extends CustomException {

    public FileUploadFailedException() {
        super(ErrorCode.FILE_UPLOAD_FAILED);
    }
}
