package com.loadbalancing.kiosk.domain.product.service;


import com.loadbalancing.kiosk.domain.product.dto.response.ProductResponse;
import com.loadbalancing.kiosk.domain.product.entity.Product;
import com.loadbalancing.kiosk.domain.product.entity.ProductImg;
import com.loadbalancing.kiosk.domain.product.repository.ProductImgRepository;
import com.loadbalancing.kiosk.domain.product.repository.ProductRepository;
import com.loadbalancing.kiosk.global.exception.custom.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImgRepository productImgRepository;

    public Product createProduct(
            String title,
            String description,
            int price,
            int stock,
            String thumbnail,
            List<String> imgs
    ) {

        Product product = Product.builder()//product 저장, 썸네일 컬럼까지만
                .title(title)
                .description(description)
                .price(price)
                .stock(stock)
                .thumbnail(thumbnail)
                .build();
        Product savedProduct = productRepository.save(product);

        List<ProductImg> newImgs = imgs.stream()//이미지들은 별도로 저장
                .map(url -> ProductImg.builder()
                        .product(product)
                        .url(url)
                        .build())
                .toList();
        productImgRepository.saveAll(newImgs);

        return savedProduct;
    }

    public Page<ProductResponse.ProductInfo> list(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(ProductResponse.ProductInfo::from);
    }

    public ProductResponse.ProductInfo findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return ProductResponse.ProductInfo.from(product);
    }
}