package com.example.movra.bc.planning.exam_schedule.domain.type;

public enum SeasonMode {
    SUNUNG_INTENSIVE,
    NAESIN_INTENSIVE,
    MOPYUNG_FOCUSED,
    BASELINE_MODE;

    public static SeasonMode determine(ExamType examType, long daysUntil) {
        if (examType == null || daysUntil < 0) {
            return BASELINE_MODE;
        }

        return switch (examType) {
            case SUNUNG -> daysUntil <= 30 ? SUNUNG_INTENSIVE : BASELINE_MODE;
            case NAESIN -> daysUntil <= 14 ? NAESIN_INTENSIVE : BASELINE_MODE;
            case MOPYUNG, HAKPYUNG -> daysUntil <= 7 ? MOPYUNG_FOCUSED : BASELINE_MODE;
            case OTHER -> BASELINE_MODE;
        };
    }
}
