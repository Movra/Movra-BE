package com.example.movra.bc.insight.behavior_insight.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.ReflectionTextView;

import java.time.LocalDate;
import java.util.List;

/**
 * feedback BC(DailyReflection, TinyWin) 조회 포트(ACL).
 */
public interface ReflectionReadPort {

    int countReflections(UserId userId, LocalDate from, LocalDate to);

    int countTinyWins(UserId userId, LocalDate from, LocalDate to);

    List<ReflectionTextView> findReflectionTexts(UserId userId, LocalDate from, LocalDate to);
}
