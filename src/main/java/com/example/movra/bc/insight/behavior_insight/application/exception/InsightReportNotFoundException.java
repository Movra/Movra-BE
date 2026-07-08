package com.example.movra.bc.insight.behavior_insight.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InsightReportNotFoundException extends CustomException {

    public InsightReportNotFoundException() {
        super(ErrorCode.INSIGHT_REPORT_NOT_FOUND);
    }
}
