package com.loadbalancing.kiosk.domain.admin.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AdminProductUpdateRequest(

        @NotBlank(message = "상품명은 필수입니다.")
        String title,

        @NotBlank(message = "상품 설명은 필수입니다.")
        String description,

        @NotNull(message = "가격은 필수입니다.")
        @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
        Integer price,

        String thumbnail
) {
}