package com.example.movra.bc.accountability.accountability_relation.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class MonitoringTargetNotAllowedException extends CustomException {

    public MonitoringTargetNotAllowedException() {
        super(ErrorCode.MONITORING_TARGET_NOT_ALLOWED);
    }
}
