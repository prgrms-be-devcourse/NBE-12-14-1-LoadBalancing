package com.loadbalancing.kiosk.global.R3;


import com.loadbalancing.kiosk.global.exception.custom.EmptyFileException;
import com.loadbalancing.kiosk.global.exception.custom.MaxUploadSizeExceedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ImageUploadService {

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket}")
    private String bucketName;

    @Value("${cloudflare.r2.url}")
    private String publicUrl;

    // 최대 허용 기준치 (5MB = 5 * 1024 * 1024 bytes)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public String uploadImage(MultipartFile file) {

        if (file.isEmpty()) {
            throw new EmptyFileException();
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new MaxUploadSizeExceedException();
        }

        //고유한 파일명 생성 (예: uuid-원본이름.jpg) (중복 방지)
        String originalFilename = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID() + "-" + originalFilename;

        try {
            //R2에 업로드할 메타데이터 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFileName)
                    .contentType(file.getContentType())
                    .build();

            //파일 스트림을 R2로 전송
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return publicUrl + "/" + uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
        }
    }
}
