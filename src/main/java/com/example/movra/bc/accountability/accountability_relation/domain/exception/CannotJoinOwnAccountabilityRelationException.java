package com.example.movra.bc.accountability.accountability_relation.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class CannotJoinOwnAccountabilityRelationException extends CustomException {
    public CannotJoinOwnAccountabilityRelationException() {
        super(ErrorCode.CANNOT_JOIN_OWN_ACCOUNTABILITY_RELATION);
    }
}
