package com.example.movra.bc.accountability.accountability_relation.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class NotSubjectUserException extends CustomException {

    public NotSubjectUserException() {
        super(ErrorCode.NOT_SUBJECT_USER);
    }
}
