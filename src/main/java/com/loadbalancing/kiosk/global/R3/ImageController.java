package com.loadbalancing.kiosk.global.R3;

import com.loadbalancing.kiosk.global.ApiResponse;
import com.loadbalancing.kiosk.global.R3.response.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/images")
public class ImageController {

    private final ImageUploadService imageUploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadResponse>> uploadImage(@RequestParam("file") MultipartFile file) {
        // 서비스에서 업로드 후 URL 반환
        String imageUrl = imageUploadService.uploadImage(file);

        //DTO 객체로 감싸서 응답
        UploadResponse responseImage = UploadResponse.from(imageUrl);
        return ResponseEntity.status(201).body(ApiResponse.success(
                201,
                responseImage
                
        ));
    }
}
