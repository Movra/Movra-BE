package com.example.movra.bc.insight.behavior_insight.domain.event;

import com.example.movra.bc.account.user.domain.user.vo.UserId;

import java.util.List;

/**
 * 선언 선호 vs 실제 행동의 괴리가 감지되었을 때 발행. personalization BC가 수신해
 * 조정 제안을 기록한다(자동 반영 금지, 사용자 수락 시에만 적용).
 */
public record ProfileDriftDetectedEvent(
        UserId userId,
        List<ProfileDriftItem> items
) {}
