package com.example.movra.bc.planning.timetable.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.UUID;

public record AddDirectSlotRequest(

        @NotBlank(message = "content는 필수입니다.")
        @Size(max = 255, message = "content는 255자 이내로 작성해주세요.")
        String content,

        @NotNull(message = "시작 시간은 필수입니다.")
        LocalTime startTime,

        @NotNull(message = "종료 시간은 필수입니다.")
        LocalTime endTime
) {}
