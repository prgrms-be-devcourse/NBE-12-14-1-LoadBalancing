package com.loadbalancing.kiosk.domain.product.entity;

import com.loadbalancing.kiosk.global.entity.BaseSoftDeleteTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Builder
@Table(name = "product")
@SQLDelete(sql = "UPDATE product SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Product extends BaseSoftDeleteTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    @Column(name = "thumbnail", nullable = false)
    private String thumbnail;

    public void decreaseStock(int quantity){
        if(quantity <= 0){
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
            //예외 만들어야하나?
        }
        if(this.stock < quantity){
            throw new IllegalArgumentException("재고가 부족합니다.");
            //예외?만들기?
        }
        this.stock -= quantity;

    }
}
