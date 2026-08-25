package com.loadbalancing.kiosk.domain.product.controller;


import com.loadbalancing.kiosk.domain.product.entity.Product;
import com.loadbalancing.kiosk.domain.product.service.ProductService;
import com.loadbalancing.kiosk.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/")
public class ProductController {

    private final ProductService productService;

    @GetMapping("auth/product/list")
    public ResponseEntity<ApiResponse<?>> list(){
        List<Product> products = productService.list();

        return ResponseEntity.ok(ApiResponse.success(
                200,
                products
        ));
    }
}
