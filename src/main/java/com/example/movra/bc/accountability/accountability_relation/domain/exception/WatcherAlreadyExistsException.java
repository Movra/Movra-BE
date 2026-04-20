package com.example.movra.bc.accountability.accountability_relation.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class WatcherAlreadyExistsException extends CustomException {

    public WatcherAlreadyExistsException() {
        super(ErrorCode.WATCHER_ALREADY_EXISTS);
    }
}
