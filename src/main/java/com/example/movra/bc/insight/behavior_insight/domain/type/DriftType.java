package com.example.movra.bc.insight.behavior_insight.domain.type;

/**
 * 선언된 선호(BehaviorProfile)와 실제 행동 사이에서 감지되는 괴리의 종류.
 * enum 이름은 personalization의 ProfileAdjustmentTarget과 1:1로 매핑된다(name 기준).
 */
public enum DriftType {
    FOCUS_HOURS,
    EXECUTION_DIFFICULTY,
    RECOVERY_STYLE
}
