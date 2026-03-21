package com.example.movra.sharedkernel.file.storage;

import com.example.movra.sharedkernel.file.storage.type.ImageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageHelper {

    private final ImageFileStorageService imageFileStorageService;

    public String upload(MultipartFile file, ImageType imageType) {
        return imageFileStorageService.upload(file, imageType);
    }

    public String update(String oldUrl, MultipartFile newFile, ImageType imageType){
        return imageFileStorageService.update(oldUrl, newFile, imageType);
    }

    public void cleanup(String imageUrl) {
        try {
            imageFileStorageService.deleteByKey(imageUrl);
        } catch (Exception e) {
            log.warn("이미지 삭제 실패 (고아 파일 발생 가능): {}", imageUrl);
        }
    }
}
