package com.loadbalancing.kiosk.domain.product.service;


import com.loadbalancing.kiosk.domain.product.dto.ProductRequest;
import com.loadbalancing.kiosk.domain.product.dto.ProductResponse;
import com.loadbalancing.kiosk.domain.product.dto.ProductSearchRequest;
import com.loadbalancing.kiosk.domain.product.infra.entity.Product;
import com.loadbalancing.kiosk.domain.product.infra.entity.ProductImg;
import com.loadbalancing.kiosk.domain.product.infra.repository.ProductImgRepository;
import com.loadbalancing.kiosk.domain.product.infra.repository.ProductRepository;
import com.loadbalancing.kiosk.global.exception.custom.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImgRepository productImgRepository;

    @Transactional
    public ProductResponse.ProductInfo createProduct(ProductRequest.ProductCreate productRequest) {

        Product product = Product.builder()//product 저장, 썸네일 컬럼까지만
            .title(productRequest.title())
            .description(productRequest.description())
            .price(productRequest.price())
            .stock(productRequest.stock())
            .thumbnail(productRequest.thumbnail())
            .build();
        Product savedProduct = productRepository.save(product);

        List<ProductImg> newImgs = productRequest.imgs().stream()//이미지들은 별도로 저장
            .map(url -> ProductImg.builder()
                .product(product)
                .url(url)
                .build())
            .toList();
        productImgRepository.saveAll(newImgs);

        return ProductResponse.ProductInfo.from(savedProduct, newImgs);
    }

    // 상품 가격 범위 검색 시, maxPrice 미지정(null/0)일 때 사실상 무제한으로 취급하기 위한 상한값
    private static final int MAX_PRICE_DEFAULT = 99_999_999;

    @Transactional(readOnly = true)
    public Page<ProductResponse.ProductInfo> getProductsList(
            ProductSearchRequest searchRequest,
            Pageable pageable
    ) {
        // 키워드/가격 둘 다 null이든 하나만 오든 상관없이 항상 같은 쿼리 하나로 처리.
        // - keyword가 null/빈 값이면 "" 로 대체 (Containing이라 ""는 전체 매치)
        // - minPrice가 null이면 0으로, maxPrice가 null/0이면 사실상 무제한으로 대체
        String keyword = searchRequest.keyword() == null ? "" : searchRequest.keyword().trim();
        Integer minPrice = searchRequest.minPrice() == null ? 0 : searchRequest.minPrice();
        Integer maxPrice = (searchRequest.maxPrice() == null || searchRequest.maxPrice() == 0)
                ? MAX_PRICE_DEFAULT
                : searchRequest.maxPrice();

        Page<Product> products = productRepository.findByTitleContainingIgnoreCaseAndPriceBetween(
                keyword, minPrice, maxPrice, pageable
        );

        return products.map(ProductResponse.ProductInfo::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse.ProductInfo getProduct(Long id) {
        Product product = findProduct(id);

        List<ProductImg> imgList = productImgRepository.findAllByProduct(product);

        return ProductResponse.ProductInfo.from(product, imgList);
    }

    @Transactional
    public ProductResponse.ProductInfo updateProduct(Long productId, ProductRequest.ProductUpdate request) {
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
        productImgRepository.deleteAllByProductId(product.getId());

        // 요청받은 이미지 URL로 새로운 이미지 엔터티를 만든다.
        List<ProductImg> productImages = request.imageUrls().stream()
            .map(url -> ProductImg.builder()
                .product(product)
                .url(url)
                .build())
            .toList();

        List<ProductImg> savedImages =
            productImgRepository.saveAll(productImages);

        return ProductResponse.ProductInfo.from(product, savedImages);
    }

    @Transactional
    public void updateStock(
        Long productId,
        ProductRequest.StockUpdate request
    ) {
        Product product = findProduct(productId);

        // 재고만 변경하므로 이미지는 조회하지 않는다.
        product.updateStock(request.stock());
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findProduct(productId);
        productRepository.delete(product);
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() ->
                new ProductNotFoundException(productId)
            );
    }
}
