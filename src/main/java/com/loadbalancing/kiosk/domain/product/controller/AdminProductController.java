package com.loadbalancing.kiosk.domain.product.controller;

import com.loadbalancing.kiosk.domain.product.dto.ProductRequest;
import com.loadbalancing.kiosk.domain.product.dto.ProductResponse;
import com.loadbalancing.kiosk.domain.product.service.ProductService;
import com.loadbalancing.kiosk.global.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/product")
public class AdminProductController {

    private final ProductService productService;

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<ProductResponse.ProductInfo>> createProduct(
        @Valid @RequestBody ProductRequest.ProductCreate productRequest){
        ProductResponse.ProductInfo products = productService.createProduct(productRequest);

        return ResponseEntity.status(201).body(ApiResponse.success(
            201,
            products
        ));
    }

    @PutMapping("/{productId}/stock")
    public ResponseEntity<ApiResponse<Void>> updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody
            ProductRequest.StockUpdate request
    ) {
        productService.updateStock(productId, request);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContentSuccess());  // 추후 응답에 관해 확인 필요
    }



    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse.ProductInfo>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody
            ProductRequest.ProductUpdate request
    ) {
        ProductResponse.ProductInfo response = productService.updateProduct(productId, request);

        return ResponseEntity.ok(
                ApiResponse.success(200, response)
        );
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long productId
    ) {
        productService.deleteProduct(productId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContentSuccess()); // 추후 응답에 관해 확인 필요
    }
}