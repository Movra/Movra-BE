package com.example.movra.config.cache;

import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class HomeCacheKey {

    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    public String currentUserId() {
        return currentUserQuery.currentUser().userId().id().toString();
    }

    public String currentUserIdToday() {
        return currentUserId() + ":" + LocalDate.now(clock);
    }

}
