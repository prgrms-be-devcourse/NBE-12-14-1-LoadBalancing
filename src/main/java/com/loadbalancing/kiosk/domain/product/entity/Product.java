package com.loadbalancing.kiosk.domain.product.entity;

import com.loadbalancing.kiosk.global.entity.BaseSoftDeleteTimeEntity;
import com.loadbalancing.kiosk.global.exception.custom.InsufficientStockException;
import com.loadbalancing.kiosk.global.exception.custom.InvalidStockQuantityException;
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

    public void update(
            String title,
            String description,
            int price,
            String thumbnail
    ) {
        this.title = title;
        this.description = description;
        this.price = price;

        if (thumbnail != null) {
            this.thumbnail = thumbnail;
        }
    }

    public void updateStock(int stock) {
        this.stock = stock;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new InvalidStockQuantityException(quantity);
        }
        if (this.stock < quantity) {
            throw new InsufficientStockException(
                    this.id,
                    this.stock,
                    quantity
            );
        }
        this.stock -= quantity;
    }
}
