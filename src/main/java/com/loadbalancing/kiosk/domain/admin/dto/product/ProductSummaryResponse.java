package com.loadbalancing.kiosk.domain.admin.dto.product;

import lombok.Builder;

import java.util.List;

//상품/재고 정보를 전체적으로 감싸서 전달해주는 dto
@Builder
public record ProductSummaryResponse(
        long totalProductCount,
        long outOfStockProductCount,
        long lowStockProductCount,
        long deletedProductCount,
        List<RecentProductResponse> recentProducts,
        List<ProductStockResponse> stockStatus
) {
}