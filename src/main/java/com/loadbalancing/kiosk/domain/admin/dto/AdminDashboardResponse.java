package com.loadbalancing.kiosk.domain.admin.dto;
import com.loadbalancing.kiosk.domain.product.dto.response.ProductResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record AdminDashboardResponse(
        long totalProductCount,
        List<ProductResponse.ProductInfo> outOfStockProducts,
        List<ProductResponse.ProductInfo> lowStockProducts,
        List<ProductResponse.ProductInfo> recentProducts
) {
}