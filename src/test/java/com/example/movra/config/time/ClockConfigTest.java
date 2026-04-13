package com.example.movra.config.time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Clock;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class ClockConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ClockConfig.class);

    @Test
    @DisplayName("clock uses Asia/Seoul by default")
    void clock_defaultZone_success() {
        contextRunner.run(context -> {
            Clock clock = context.getBean(Clock.class);

            assertThat(clock.getZone()).isEqualTo(ZoneId.of("Asia/Seoul"));
        });
    }

    @Test
    @DisplayName("clock uses configured zone when overridden")
    void clock_overrideZone_success() {
        contextRunner
                .withPropertyValues("app.time.zone=UTC")
                .run(context -> {
                    Clock clock = context.getBean(Clock.class);

                    assertThat(clock.getZone()).isEqualTo(ZoneId.of("UTC"));
                });
    }
}
