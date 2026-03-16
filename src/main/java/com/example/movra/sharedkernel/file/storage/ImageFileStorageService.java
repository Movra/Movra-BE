package com.example.movra.sharedkernel.file.storage;

import com.example.movra.sharedkernel.file.storage.exception.FileDeleteFailedException;
import com.example.movra.sharedkernel.file.storage.exception.FileUploadFailedException;
import com.example.movra.sharedkernel.file.storage.exception.ImageNotFoundException;
import com.example.movra.sharedkernel.file.storage.exception.InvalidFileExtensionException;
import com.example.movra.sharedkernel.file.storage.type.ImageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.net.URI;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageFileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png");

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png");

    private final S3Client s3Client;

    @Value("${cloud.file.seaweedfs.bucket}")
    private String bucket;

    @Value("${cloud.file.seaweedfs.url-prefix}")
    private String urlPrefix;



    public String upload(MultipartFile file, ImageType imageType){
        validateFile(file);

        String extension = extractExtension(file.getOriginalFilename());
        String key = generateSafeKey(extension, imageType);

        try(InputStream inputStream = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(inputStream, file.getSize())
            );

            return buildPublicUrl(key);
        } catch (Exception e) {
            log.error("파일 업로드에 실패했습니다. : ", e);
            throw new FileUploadFailedException();
        }
    }

    public void deleteByKey(String url) {
        String key = extractKeyFromUrl(url);
        validateKey(key);

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
        } catch (Exception e) {
            log.error("파일 삭제에 실패했습니다. : ", e);
            throw new FileDeleteFailedException();
        }
    }

    public String update(String oldUrl, MultipartFile newFile, ImageType imageType) {
        if (newFile == null || newFile.isEmpty()) {
            return oldUrl;
        }

        String newUrl = upload(newFile, imageType);

        if (oldUrl != null) {
            try {
                deleteByKey(oldUrl);
            } catch (Exception e) {
                log.warn("삭제 실패, 고아 파일 발생 가능: {}", oldUrl, e);
            }
        }

        return newUrl;
    }

    //파일 유효성 검사 메서드
    private void validateFile(MultipartFile file){

        if (file == null || file.isEmpty()) {
            throw new ImageNotFoundException();
        }

        if(!ALLOWED_CONTENT_TYPES.contains(file.getContentType())){
            throw new InvalidFileExtensionException();
        }

        String extension = extractExtension(file.getOriginalFilename());
        if(!ALLOWED_EXTENSIONS.contains(extension)){
            throw new InvalidFileExtensionException();
        }
    }

    //파일 확장자 추출 메서드
    private String extractExtension(String filename){
        if(filename == null || !filename.contains(".")){
            throw new InvalidFileExtensionException();
        }

        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    //이미지 서버에 저장되는 안전한 키 값 메서드
    private String generateSafeKey(String extension, ImageType imageType){
        return imageType.getPrefix() + UUID.randomUUID() + "." + extension;
    }

    //파일을 key 기반으로 접근 가능한 Public URL 생성 메서드
    private String buildPublicUrl(String key) {
        return urlPrefix.endsWith("/")
                ? urlPrefix + key
                : urlPrefix + "/" + key;
    }

    //파일의 Public URL을 파싱하여 실제 object key를 추출하는 메서드
    private String extractKeyFromUrl(String url){
        try{
            var uri = URI.create(url);
            String path = uri.getPath();

            if (path == null || path.length() <= 1) {
                throw new FileDeleteFailedException();
            }

            return path.substring(1);
        } catch (Exception e){
            log.error("파일을 삭제할 수 없습니다. :" , e);
            throw new FileDeleteFailedException();
        }
    }

    //키 유효성 검사
    private void validateKey(String key) {
        if (key == null || key.isBlank() || key.contains("..")) {
            throw new FileDeleteFailedException();
        }
    }

}
