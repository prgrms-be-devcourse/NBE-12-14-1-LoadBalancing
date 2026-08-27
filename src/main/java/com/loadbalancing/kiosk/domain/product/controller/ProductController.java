package com.loadbalancing.kiosk.domain.product.controller;


import com.loadbalancing.kiosk.domain.product.dto.ProductResponse;
import com.loadbalancing.kiosk.domain.product.service.ProductService;
import com.loadbalancing.kiosk.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/product")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/list")//다건 조회
    public ResponseEntity<ApiResponse<?>> getProducts(
            @RequestParam(value = "keyword", required = false) String keyword,//검색 문자열, 공백이면 전체 반환
            @PageableDefault(
                    sort = "id",
                    direction = Sort.Direction.DESC) Pageable pageable//10개 단위 페이징
    ){
        Page<ProductResponse.ProductInfo> products = productService.getProductsList(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(
                200,
                products
        ));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ApiResponse<?>> getProduct(@PathVariable Long id) {
        ProductResponse.ProductInfo product = productService.getProduct(id);

        return ResponseEntity.ok(ApiResponse.success(
                200,
                product
        ));
    }


}
