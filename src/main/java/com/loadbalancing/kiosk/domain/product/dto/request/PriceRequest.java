package com.loadbalancing.kiosk.domain.product.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PriceRequest(

        @PositiveOrZero(message = "음수는 들어갈 수 없습니다.")
        @NotNull(message = "최소 가격을 입력해주세요.")
        Long minPrice,

        @PositiveOrZero(message = "음수는 들어갈 수 없습니다.")
        @NotNull(message = "최대 가격을 입력해주세요.")
        Long maxPrice
) {

}
