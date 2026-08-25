package com.loadbalancing.kiosk.domain.admin.product.controller;

import com.loadbalancing.kiosk.domain.admin.product.dto.AdminProductResponse;
import com.loadbalancing.kiosk.domain.admin.product.dto.AdminProductStockUpdateRequest;
import com.loadbalancing.kiosk.domain.admin.product.dto.AdminProductUpdateRequest;
import com.loadbalancing.kiosk.domain.admin.product.service.AdminProductService;
import com.loadbalancing.kiosk.global.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/product")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @PutMapping("/{productId}/stock")
    public ResponseEntity<ApiResponse<AdminProductResponse>> updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody AdminProductStockUpdateRequest request
    ) {
        AdminProductResponse response =
                adminProductService.updateStock(productId, request);

        return ResponseEntity.ok(ApiResponse.success(200, response));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<AdminProductResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody AdminProductUpdateRequest request
    ) {
        AdminProductResponse response =
                adminProductService.updateProduct(productId, request);

        return ResponseEntity.ok(ApiResponse.success(200, response));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long productId
    ) {
        adminProductService.deleteProduct(productId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContentSuccess());
    }
}