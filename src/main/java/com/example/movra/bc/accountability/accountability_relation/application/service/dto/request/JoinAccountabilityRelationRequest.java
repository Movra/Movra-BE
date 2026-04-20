package com.example.movra.bc.accountability.accountability_relation.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinAccountabilityRelationRequest(

        @NotBlank(message = "inviteCode는 필수입니다.")
        @Size(max = 10, message = "inviteCode를 10 사이로 입력해주세요.")
        String inviteCode
) {
}
