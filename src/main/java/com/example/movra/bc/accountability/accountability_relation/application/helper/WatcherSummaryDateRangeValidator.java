package com.example.movra.bc.accountability.accountability_relation.application.helper;

import com.example.movra.bc.accountability.accountability_relation.application.service.exception.InvalidDateRangeException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class WatcherSummaryDateRangeValidator {

    public void validate(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new InvalidDateRangeException();
        }
    }
}
