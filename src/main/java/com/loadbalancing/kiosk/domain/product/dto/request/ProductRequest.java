package com.loadbalancing.kiosk.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record ProductRequest(

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

        @NotEmpty(message = "첨부사진들을 등록해주세요.")
        List<@NotBlank(message = "url을 입력해주세요.") String> imgs
) {

}
