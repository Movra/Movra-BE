package com.example.movra.config.time;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${app.time.zone:Asia/Seoul}") String timeZone) {
        return Clock.system(ZoneId.of(timeZone));
    }
}
