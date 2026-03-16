package com.example.morva.bc.account.application.user.helper;

import com.example.morva.sharedkernel.file.storage.ImageFileStorageService;
import com.example.morva.sharedkernel.file.storage.type.ImageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileImageHelper {

    private final ImageFileStorageService imageFileStorageService;

    public String upload(MultipartFile file) {
        return imageFileStorageService.upload(file, ImageType.PROFILE);
    }

    public void cleanup(String profileUrl) {
        try {
            imageFileStorageService.deleteByKey(profileUrl);
        } catch (Exception e) {
            log.warn("프로필 이미지 삭제 실패 (고아 파일 발생 가능): {}", profileUrl);
        }
    }
}
