package com.example.movra.bc.visioning.future_vision.application.service.dto.request;

import com.example.movra.sharedkernel.validation.NotEmptyMultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record UpdateWeeklyVisionRequest(

        @NotEmptyMultipartFile
        MultipartFile weeklyVisionImageUrl
) {
}
