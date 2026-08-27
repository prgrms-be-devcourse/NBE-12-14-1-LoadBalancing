package com.loadbalancing.kiosk.domain.product.service;


import com.loadbalancing.kiosk.domain.product.dto.request.ProductRequest;
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

    public ProductResponse.ProductInfo createProduct(ProductRequest productRequest) {

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

    public Page<ProductResponse.ProductInfo> list(String keyword, Pageable pageable) {

        Page<Product> products;//if문 안에서 선언하면 안되기 때문에 밖에서 선언

        if(keyword == null || keyword.isEmpty()){
            products = productRepository.findAll(pageable);
        } else {
            products = productRepository.findByTitleContainingIgnoreCase(keyword.trim(), pageable);
        }
        return products.map(ProductResponse.ProductInfo::from);
    }

    public ProductResponse.ProductInfo findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        List<ProductImg> imgList = productImgRepository.findAllByProduct(product);

        return ProductResponse.ProductInfo.from(product, imgList);
    }
}