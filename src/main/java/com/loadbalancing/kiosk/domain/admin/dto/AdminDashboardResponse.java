package com.loadbalancing.kiosk.domain.admin.dto;

import com.loadbalancing.kiosk.domain.admin.dto.product.ProductSummaryResponse;
import lombok.Builder;

@Builder
public record AdminDashboardResponse(
        //상품/재고 용 dto
        ProductSummaryResponse productSummary
) {
}