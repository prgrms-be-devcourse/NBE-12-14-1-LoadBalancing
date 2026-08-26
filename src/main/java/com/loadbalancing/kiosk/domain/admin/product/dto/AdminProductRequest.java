package com.loadbalancing.kiosk.domain.admin.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public class AdminProductRequest {

    private AdminProductRequest() {
    }

    public record UpdateRequest(

            @NotBlank(message = "상품명은 필수입니다.")
            String title,

            @NotBlank(message = "상품 설명은 필수입니다.")
            String description,

            @NotNull(message = "가격은 필수입니다.")
            @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
            Integer price,

            @NotBlank(message = "썸네일은 필수입니다.")
            String thumbnail,

            @NotNull(message = "상품 이미지 목록은 필수입니다.")
            @Valid
            List<ImageUpdateRequest> images
    ) {
    }

    public record ImageUpdateRequest(

            @NotNull(message = "상품 이미지 ID는 필수입니다.")
            Long id,

            @NotBlank(message = "상품 이미지 주소는 필수입니다.")
            String url
    ) {
    }

    public record StockUpdateRequest(

            @NotNull(message = "재고는 필수입니다.")
            @PositiveOrZero(message = "재고는 0 이상이어야 합니다.")
            Integer stock
    ) {
    }
}