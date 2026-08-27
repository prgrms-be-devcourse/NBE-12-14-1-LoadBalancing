package com.loadbalancing.kiosk.domain.product.controller;


import com.loadbalancing.kiosk.domain.product.dto.request.PriceRequest;
import com.loadbalancing.kiosk.domain.product.dto.request.ProductRequest;
import com.loadbalancing.kiosk.domain.product.dto.response.ProductResponse;
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

    @GetMapping("/auth/product/list")//다건 조회
    public ResponseEntity<ApiResponse<?>> list(
            @RequestParam(value = "keyword", required = false) String keyword,//검색 문자열, 공백이면 전체 반환
            @PageableDefault(
                    sort = "id",
                    direction = Sort.Direction.DESC) Pageable pageable//10개 단위 페이징
    ){
        Page<ProductResponse.ProductInfo> products = productService.list(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(
                200,
                products
        ));
    }

    @GetMapping("/auth/product/list/price")
    public ResponseEntity<ApiResponse<?>> listByPrice(
            @Valid @ModelAttribute PriceRequest priceRequest,
            @PageableDefault(size = 10, sort = "price", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<ProductResponse.ProductInfo> result = productService.listByPrice(priceRequest, pageable);
        return ResponseEntity.ok(ApiResponse.success(
                200,
                result
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

    @PostMapping("/product")
    @Transactional
    public ResponseEntity<ApiResponse<?>> create(
            @Valid @RequestBody ProductRequest productRequest){
        ProductResponse.ProductInfo products = productService.createProduct(productRequest);

        return ResponseEntity.status(201).body(ApiResponse.success(
                201,
                products
        ));
    }
}
