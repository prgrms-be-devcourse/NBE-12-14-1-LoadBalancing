package com.loadbalancing.kiosk.domain.admin.product.controller;

import com.loadbalancing.kiosk.domain.admin.product.dto.AdminProductRequest;
import com.loadbalancing.kiosk.domain.admin.product.dto.AdminProductResponse;
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
    public ResponseEntity<ApiResponse<Void>> updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody
            AdminProductRequest.StockUpdateRequest request
    ) {
        adminProductService.updateStock(productId, request);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContentSuccess());  // 추후 응답에 관해 확인 필요
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<AdminProductResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody
            AdminProductRequest.UpdateRequest request
    ) {
        AdminProductResponse response =
                adminProductService.updateProduct(productId, request);

        return ResponseEntity.ok(
                ApiResponse.success(200, response)
        );
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long productId
    ) {
        adminProductService.deleteProduct(productId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContentSuccess()); // 추후 응답에 관해 확인 필요
    }
}