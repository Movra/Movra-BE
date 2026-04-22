package com.example.movra.bc.personalization.behavior_profile.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExecutionDifficulty {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int maxTopPicks;
}
