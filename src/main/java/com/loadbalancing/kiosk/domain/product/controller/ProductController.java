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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        Page<ProductResponse.ListDto> products = productService.list(pageable);
        return ResponseEntity.ok(ApiResponse.success(
                200,
                products
        ));
    }

    @GetMapping("/auth/product/detail/{id}")
    public ResponseEntity<ApiResponse<?>> detail(@PathVariable Long id) {
        ProductResponse.DetailDto product = productService.findById(id);

        return ResponseEntity.ok(ApiResponse.success(
                200,
                product
        ));
    }
}
