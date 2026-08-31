package com.loadbalancing.kiosk.domain.order.dto;

import com.loadbalancing.kiosk.domain.order.infra.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class OrderRequest {

    public record OrderCreate(
            @NotBlank(message = "이메일을 입력해주세요.")
            @Email(message = "올바른 이메일 형식이 아닙니다.")
            String email,

            @NotBlank(message = "주소를 입력해주세요.")
            String addressLine1,

            @NotBlank(message = "상세주소를 입력해주세요.")
            String addressLine2,

            @NotBlank(message = "우편번호를 입력해주세요.")
            @Pattern(regexp = "\\d{5}", message = "우편번호는 숫자 5자리여야 합니다.")
            String postalCode,

            @NotEmpty(message = "주문할 상품을 1개 이상 담아주세요.")
            @Valid
            List<OrderItem> items
    ) {}

    public record OrderItem(
            @NotNull(message = "상품을 선택해주세요.")
            Long productId,

            @NotNull(message = "수량을 입력해주세요.")
            @Positive(message = "수량은 1개 이상이어야 합니다.")
            Long quantity
    ) {}

    public record AdminOrderRequest(
        @NotNull OrderStatus status
    ) {}
}
