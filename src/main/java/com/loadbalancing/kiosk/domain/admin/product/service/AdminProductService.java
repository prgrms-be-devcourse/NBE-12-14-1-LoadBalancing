package com.loadbalancing.kiosk.domain.admin.product.service;

import com.loadbalancing.kiosk.domain.admin.product.dto.AdminProductRequest;
import com.loadbalancing.kiosk.domain.admin.product.dto.AdminProductResponse;
import com.loadbalancing.kiosk.domain.admin.product.repository.AdminProductImgRepository;
import com.loadbalancing.kiosk.domain.admin.product.repository.AdminProductRepository;
import com.loadbalancing.kiosk.domain.product.entity.Product;
import com.loadbalancing.kiosk.domain.product.entity.ProductImg;
import com.loadbalancing.kiosk.global.exception.custom.ProductImgNotFoundException;
import com.loadbalancing.kiosk.global.exception.custom.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        product.update(
                request.title(),
                request.description(),
                request.price(),
                request.thumbnail()
        );

        // 현재 상품에 등록된 이미지 목록을 조회한다.
        List<ProductImg> productImages =
                adminProductImgRepository
                        .findAllByProductId(productId);

        // 이미지 ID를 기준으로 기존 이미지를 찾기 위한 Map을 만든다.
        Map<Long, ProductImg> productImageMap =
                productImages.stream()
                        .collect(Collectors.toMap(
                                ProductImg::getId,
                                Function.identity()
                        ));

        // 요청으로 받은 이미지 ID에 해당하는 기존 이미지 URL을 변경한다.
        for (AdminProductRequest.ImageUpdateRequest imageRequest
                : request.images()) {

            ProductImg productImg =
                    productImageMap.get(imageRequest.id());

            // 존재하지 않거나 다른 상품에 속한 이미지라면 404 처리한다.
            if (productImg == null) {
                throw new ProductImgNotFoundException(
                        imageRequest.id()
                );
            }

            productImg.updateUrl(imageRequest.url());
        }

        // Product와 ProductImg 모두 JPA 변경 감지로 UPDATE된다.
        return AdminProductResponse.from(
                product,
                productImages
        );
    }

    @Transactional
    public AdminProductResponse updateStock(
            Long productId,
            AdminProductRequest.StockUpdateRequest request
    ) {
        Product product = findProduct(productId);

        product.updateStock(request.stock());

        List<ProductImg> productImages =
                adminProductImgRepository
                        .findAllByProductId(productId);

        return AdminProductResponse.from(
                product,
                productImages
        );
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findProduct(productId);

        // Product의 @SQLDelete에 의해 deleted_at이 갱신된다.
        adminProductRepository.delete(product);
    }

    private Product findProduct(Long productId) {
        return adminProductRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId)
                );
    }
}