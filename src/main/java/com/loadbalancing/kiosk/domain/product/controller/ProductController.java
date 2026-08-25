package com.loadbalancing.kiosk.domain.product.controller;


import com.loadbalancing.kiosk.domain.product.dto.request.ProductRequest;
import com.loadbalancing.kiosk.domain.product.dto.response.ProductResponse;
import com.loadbalancing.kiosk.domain.product.entity.Product;
import com.loadbalancing.kiosk.domain.product.service.ProductService;
import com.loadbalancing.kiosk.global.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/auth/product/list")
    public ResponseEntity<ApiResponse<?>> list(
            @PageableDefault(
                    sort = "id",
                    direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<ProductResponse.ProductInfo> products = productService.list(pageable);
        return ResponseEntity.ok(ApiResponse.success(
                200,
                products
        ));
    }

    @GetMapping("/auth/product/detail/{id}")
    public ResponseEntity<ApiResponse<?>> detail(@PathVariable Long id) {
        ProductResponse.ProductInfo product = productService.findById(id);

        return ResponseEntity.ok(ApiResponse.success(
                200,
                product
        ));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<?>> create(
            @Valid @RequestBody ProductRequest productRequest){
        Product product = productService.createProduct(
                productRequest.title(),
                productRequest.description(),
                productRequest.price(),
                productRequest.stock(),
                productRequest.thumbnail(),
                productRequest.imgs());

        return ResponseEntity.status(201).body(ApiResponse.success(
                201,
                product
        ));
    }
}
