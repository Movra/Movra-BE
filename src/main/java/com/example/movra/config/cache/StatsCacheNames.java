package com.example.movra.config.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatsCacheNames {
    public static final String FOCUS_STATS_DAILY = "stats:v1:focus-daily";
    public static final String FOCUS_STATS_WEEKLY = "stats:v1:focus-weekly";
    public static final String FOCUS_STATS_MONTHLY = "stats:v1:focus-monthly";
}
