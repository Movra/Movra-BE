package com.example.movra.bc.study_room.room.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;

public record JoinRoomRequest(
        @NotBlank(message = "초대 코드는 필수입니다.")
        String inviteCode
) {
}
