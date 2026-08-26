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
            AdminProductRequest.UpdateRequest request
    ) {
        Product product = findProduct(productId);

        // 상품 기본 정보와 재고를 함께 수정한다.
        product.update(
                request.title(),
                request.description(),
                request.price(),
                request.stock(),
                request.thumbnail()
        );

        // 기존 이미지 DB 정보를 삭제한다.
        adminProductImgRepository.deleteAllByProductId(productId);

        // 요청받은 이미지 URL로 새로운 이미지 엔터티를 만든다.
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
    public void updateStock(
            Long productId,
            AdminProductRequest.StockUpdateRequest request
    ) {
        Product product = findProduct(productId);

        // 재고만 변경하므로 이미지는 조회하지 않는다.
        product.updateStock(request.stock());
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findProduct(productId);
        adminProductRepository.delete(product);
    }

    private Product findProduct(Long productId) {
        return adminProductRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId)
                );
    }
}