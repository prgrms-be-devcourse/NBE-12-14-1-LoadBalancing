package com.loadbalancing.kiosk.domain.product.dto;

import jakarta.validation.constraints.PositiveOrZero;

public record ProductSearchRequest(

        String keyword,

        @PositiveOrZero(message = "음수는 들어갈 수 없습니다.")
        Integer minPrice,

        @PositiveOrZero(message = "음수는 들어갈 수 없습니다.")
        Integer maxPrice
) {

}
