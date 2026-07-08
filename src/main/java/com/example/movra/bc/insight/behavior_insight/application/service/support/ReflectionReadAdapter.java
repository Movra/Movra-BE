package com.example.movra.bc.insight.behavior_insight.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.feedback.daily_reflection.domain.repository.DailyReflectionRepository;
import com.example.movra.bc.feedback.tiny_win.domain.repository.TinyWinRepository;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.ReflectionTextView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReflectionReadAdapter implements ReflectionReadPort {

    private final DailyReflectionRepository dailyReflectionRepository;
    private final TinyWinRepository tinyWinRepository;

    @Override
    public int countReflections(UserId userId, LocalDate from, LocalDate to) {
        return dailyReflectionRepository
                .findAllByUserIdAndReflectionDateBetween(userId, from, to)
                .size();
    }

    @Override
    public int countTinyWins(UserId userId, LocalDate from, LocalDate to) {
        return tinyWinRepository
                .findAllByUserIdAndLocalDateBetween(userId, from, to)
                .size();
    }

    @Override
    public List<ReflectionTextView> findReflectionTexts(UserId userId, LocalDate from, LocalDate to) {
        return dailyReflectionRepository
                .findAllByUserIdAndReflectionDateBetween(userId, from, to)
                .stream()
                .map(reflection -> new ReflectionTextView(
                        reflection.getWhatWentWell(),
                        reflection.getWhatBrokeDown()
                ))
                .toList();
    }
}
