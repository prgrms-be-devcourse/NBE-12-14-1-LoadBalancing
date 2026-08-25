package com.loadbalancing.kiosk.domain.product.entity;

import com.loadbalancing.kiosk.global.entity.BaseSoftDeleteTimeEntity;
import com.loadbalancing.kiosk.global.exception.custom.InsufficientStockException;
import com.loadbalancing.kiosk.global.exception.custom.InvalidStockQuantityException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Builder
@Table(name = "product")
@SQLDelete(
        sql = "UPDATE product SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?"
)
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

    /**
     * 관리자가 상품명, 설명, 가격, 썸네일을 수정한다.
     * 썸네일이 전달되지 않으면 기존 값을 유지한다.
     */
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

    /**
     * 관리자가 상품 재고를 특정 값으로 변경한다.
     */
    public void updateStock(int stock) {
        this.stock = stock;
    }

    /**
     * 주문 수량만큼 상품 재고를 차감한다.
     */
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
