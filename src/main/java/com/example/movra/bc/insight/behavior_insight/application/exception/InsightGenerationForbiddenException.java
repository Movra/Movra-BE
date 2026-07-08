package com.example.movra.bc.insight.behavior_insight.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

/**
 * 수동 분석 리포트 생성(테스트용 트리거) 권한이 없는 계정이 호출한 경우.
 */
public class InsightGenerationForbiddenException extends CustomException {

    public InsightGenerationForbiddenException() {
        super(ErrorCode.INSIGHT_GENERATION_FORBIDDEN);
    }
}
