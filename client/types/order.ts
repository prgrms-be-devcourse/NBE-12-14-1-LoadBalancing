// OrderItemRequest (백엔드)
export interface OrderItemRequest {
  productId: number;
  quantity: number;
}

// OrderCreateRequest (백엔드)
export interface OrderCreateRequest {
  email: string;
  addressLine1: string;
  addressLine2: string;
  postalCode: string;
  items: OrderItemRequest[];
}

// OrderCreateResponse (백엔드)
export interface OrderCreateResponse {
  orderId: number;
  status: string;
}

// OrderItemResponse (백엔드) - 주문 목록 안에 들어가는 아이템 하나
export interface OrderItemInfo {
  productId: number;
  title: string;
  price: number;
  quantity: number;
}

// OrderListResponse (백엔드)
export interface OrderListItem {
  orderId: number;
  email: string;
  addressLine1: string;
  addressLine2: string;
  postalCode: string;
  status: string;
  createdAt: string;
  items: OrderItemInfo[];
}

// 백엔드 OrderStatus enum이랑 1:1로 맞춘 값.
// AdminOrderRequest.status는 이 enum 이름 그대로 받음 (한글 설명 아님) 주의
export const ORDER_STATUS_OPTIONS = [
  { value: "ORDER_RECEIVED", label: "주문접수" },
  { value: "PAYMENT_COMPLETED", label: "결제완료" },
  { value: "IN_DELIVERY", label: "배송중" },
  { value: "DELIVERED", label: "배송완료" },
  { value: "CANCELLED", label: "주문취소" },
] as const;

// 목록 응답엔 한글 설명("주문접수" 등)으로 오는데, <select>에서 현재값 맞추려면
// 그 한글을 다시 enum 이름으로 되돌려야 해서 만든 역방향 조회 함수
export function statusLabelToValue(label: string): string {
  return (
    ORDER_STATUS_OPTIONS.find((o) => o.label === label)?.value ??
    ORDER_STATUS_OPTIONS[0].value
  );
}
