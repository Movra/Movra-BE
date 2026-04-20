package com.example.movra.bc.accountability.accountability_relation.application.service.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class AccountabilityRelationNotFoundException extends CustomException {

    public AccountabilityRelationNotFoundException() {
        super(ErrorCode.ACCOUNTABILITY_RELATION_NOT_FOUND);
    }
}
