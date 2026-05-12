package com.example.movra.bc.accountability.accountability_relation.application.service.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class AccountabilityRelationAlreadyExistsException extends CustomException {

    public AccountabilityRelationAlreadyExistsException() {
        super(ErrorCode.ACCOUNTABILITY_RELATION_ALREADY_EXISTS);
    }
}
