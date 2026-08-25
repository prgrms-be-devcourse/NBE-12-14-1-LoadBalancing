package com.loadbalancing.kiosk.domain.admin.product.service;

import com.loadbalancing.kiosk.domain.admin.product.dto.AdminProductResponse;
import com.loadbalancing.kiosk.domain.admin.product.dto.AdminProductStockUpdateRequest;
import com.loadbalancing.kiosk.domain.admin.product.dto.AdminProductUpdateRequest;
import com.loadbalancing.kiosk.domain.admin.product.repository.AdminProductRepository;
import com.loadbalancing.kiosk.domain.product.entity.Product;
import com.loadbalancing.kiosk.global.exception.custom.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductService {

    private final AdminProductRepository adminProductRepository;

    @Transactional
    public AdminProductResponse updateProduct(
            Long productId,
            AdminProductUpdateRequest request
    ) {
        Product product = findProduct(productId);

        product.update(
                request.title(),
                request.description(),
                request.price(),
                request.thumbnail()
        );

        return AdminProductResponse.from(product);
    }

    @Transactional
    public AdminProductResponse updateStock(
            Long productId,
            AdminProductStockUpdateRequest request
    ) {
        Product product = findProduct(productId);
        product.updateStock(request.stock());

        return AdminProductResponse.from(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findProduct(productId);
        adminProductRepository.delete(product);
    }

    private Product findProduct(Long productId) {
        return adminProductRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}