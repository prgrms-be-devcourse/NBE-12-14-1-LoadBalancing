package com.loadbalancing.kiosk.domain.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public class ProductRequest {


    public record ProductCreate(

        @NotBlank(message = "제목을 입력해주세요.")
        String title,

        @NotBlank(message = "내용을 입력해주세요.")
        String description,

        @PositiveOrZero(message = "음수는 들어갈 수 없습니다.")
        @NotNull(message = "가격을 입력해주세요.")
        Integer price,

        @PositiveOrZero(message = "음수는 들어갈 수 없습니다.")
        @NotNull(message = "재고를 입력해주세요.")
        Integer stock,

        @NotBlank(message = "썸네일을 등록해주세요.")
        String thumbnail,

        @NotEmpty(message = "첨부사진을 등록해주세요.")
        List<@NotBlank(message = "url을 입력해주세요.") String> imgs
    ) {
    }
    public record StockUpdate(

        @NotNull(message = "재고는 필수입니다.")
        @PositiveOrZero(message = "재고는 0 이상이어야 합니다.")
        Integer stock
    ) {}

    public record ProductUpdate(

        @NotBlank(message = "상품명은 필수입니다.")
        String title,

        @NotBlank(message = "상품 설명은 필수입니다.")
        String description,

        @NotNull(message = "가격은 필수입니다.")
        @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
        Integer price,

        @NotNull(message = "재고는 필수입니다.")
        @PositiveOrZero(message = "재고는 0 이상이어야 합니다.")
        Integer stock,

        @NotBlank(message = "썸네일은 필수입니다.")
        String thumbnail,

        @NotNull(message = "상품 이미지 목록은 필수입니다.")
        List<@NotBlank(message = "상품 이미지 주소는 비어 있을 수 없습니다.") String> imageUrls
    ) {
    }

}
