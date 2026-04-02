package com.example.movra.bc.study_room.room.application.service.dto.request;

import com.example.movra.bc.study_room.room.domain.vo.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
        @NotBlank(message = "방 이름은 필수입니다.")
        @Size(max = 20, message = "방 이름은 20자 이내로 작성해주세요.")
        String name,

        @NotNull(message = "공개 여부는 필수입니다.")
        Visibility visibility
) {
}
