package com.example.movra.bc.visioning.future_vision.application.service.dto.request;

import com.example.movra.sharedkernel.validation.NotEmptyMultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record CreateFutureVisionRequest(

        @NotEmptyMultipartFile
        MultipartFile weeklyVisionImageUrl,

        @NotEmptyMultipartFile
        MultipartFile yearlyVisionImageUrl,

        @NotBlank(message = "yearlyVisionDescription 를 입력해주세요")
        @Size(max = 100, message = "yearlyVisionDescription 은 100글자 이내로 작성해주세요")
        String yearlyVisionDescription
) {
}
