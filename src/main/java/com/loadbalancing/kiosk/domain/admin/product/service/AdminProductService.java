package com.loadbalancing.kiosk.domain.admin.product.service;

import com.loadbalancing.kiosk.domain.admin.product.dto.AdminProductRequest;
import com.loadbalancing.kiosk.domain.admin.product.dto.AdminProductResponse;
import com.loadbalancing.kiosk.domain.admin.product.repository.AdminProductImgRepository;
import com.loadbalancing.kiosk.domain.admin.product.repository.AdminProductRepository;
import com.loadbalancing.kiosk.domain.product.entity.Product;
import com.loadbalancing.kiosk.domain.product.entity.ProductImg;
import com.loadbalancing.kiosk.global.exception.custom.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductService {

    private final AdminProductRepository adminProductRepository;
    private final AdminProductImgRepository adminProductImgRepository;

    @Transactional
    public AdminProductResponse updateProduct(
            Long productId,
            AdminProductRequest.UpdateDto request
    ) {
        Product product = findProduct(productId);

        product.update(
                request.title(),
                request.description(),
                request.price(),
                request.thumbnail()
        );

        adminProductImgRepository.deleteAllByProductId(productId);

        List<ProductImg> productImages = request.imageUrls().stream()
                .map(url -> ProductImg.builder()
                        .product(product)
                        .url(url)
                        .build())
                .toList();

        List<ProductImg> savedImages =
                adminProductImgRepository.saveAll(productImages);

        return AdminProductResponse.from(product, savedImages);
    }

    @Transactional
    public AdminProductResponse updateStock(
            Long productId,
            AdminProductRequest.StockUpdateDto request
    ) {
        Product product = findProduct(productId);
        product.updateStock(request.stock());

        List<ProductImg> productImages =
                adminProductImgRepository.findAllByProductId(productId);

        return AdminProductResponse.from(product, productImages);
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