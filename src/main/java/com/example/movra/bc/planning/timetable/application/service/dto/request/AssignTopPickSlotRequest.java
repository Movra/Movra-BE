package com.example.movra.bc.planning.timetable.application.service.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record AssignTopPickSlotRequest(

        @NotNull(message = "시작 시간은 필수입니다.")
        LocalTime startTime,

        @NotNull(message = "종료 시간은 필수입니다.")
        LocalTime endTime
) {}
