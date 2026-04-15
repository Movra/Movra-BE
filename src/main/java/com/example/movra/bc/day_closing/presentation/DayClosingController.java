package com.example.movra.bc.day_closing.presentation;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.day_closing.application.service.ClosedBy;
import com.example.movra.bc.day_closing.application.service.DayClosingOrchestrator;
import com.example.movra.bc.day_closing.application.service.dto.request.CloseDayRequest;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;

@RestController
@RequestMapping("/days")
@RequiredArgsConstructor
public class DayClosingController {

    private final DayClosingOrchestrator dayClosingOrchestrator;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    @PostMapping("/close")
    public ResponseEntity<Void> close(@RequestBody(required = false) CloseDayRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();
        LocalDate date = (request != null && request.date() != null)
                ? request.date()
                : LocalDate.now(clock);
        dayClosingOrchestrator.closeUserDay(userId, date, ClosedBy.USER_ACTION);
        return ResponseEntity.noContent().build();
    }
}
