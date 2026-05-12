package com.example.movra.bc.accountability.accountability_relation.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class NotWatcherUserException extends CustomException {

    public NotWatcherUserException() {
        super(ErrorCode.NOT_WATCHER_USER);
    }
}
