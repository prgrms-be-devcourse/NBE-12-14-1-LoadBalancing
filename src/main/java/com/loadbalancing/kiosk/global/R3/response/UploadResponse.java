package com.loadbalancing.kiosk.global.R3.response;

import lombok.Builder;

@Builder
public record UploadResponse(
        String url
) {
    public static UploadResponse from(String url){
        return UploadResponse.builder()
                .url(url)
                .build();
    }
}
