package com.loadbalancing.kiosk.domain.product.entity;

import com.loadbalancing.kiosk.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Entity
@Builder
@Table(name = "product_img")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductImg extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String url;
}