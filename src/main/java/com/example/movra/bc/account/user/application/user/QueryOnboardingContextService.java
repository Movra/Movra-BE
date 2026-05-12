package com.example.movra.bc.account.user.application.user;

import com.example.movra.bc.account.user.application.user.dto.response.OnboardingContextResponse;
import com.example.movra.sharedkernel.notification.SchoolHoursPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueryOnboardingContextService {

    private final SchoolHoursPolicy schoolHoursPolicy;

    public OnboardingContextResponse query() {
        return OnboardingContextResponse.builder()
                .pendingSchoolHours(schoolHoursPolicy.isSchoolHoursNow())
                .build();
    }
}
