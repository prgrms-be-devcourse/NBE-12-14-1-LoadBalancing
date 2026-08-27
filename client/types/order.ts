// OrderRequest.OrderItem (백엔드)
export interface OrderItemRequest {
  productId: number;
  quantity: number;
}

// OrderRequest.OrderCreate (백엔드)
export interface OrderCreateRequest {
  email: string;
  addressLine1: string;
  addressLine2: string;
  postalCode: string;
  items: OrderItemRequest[];
}

// OrderResponse.OrderItemInfo (백엔드) - 주문 안에 들어가는 아이템 하나
export interface OrderItemInfo {
  productId: number;
  title: string;
  price: number;
  quantity: number;
}

// OrderResponse.OrderInfo (백엔드) - 생성/목록/조회/검색 응답이 전부 이 모양 하나로 통일됨.
// 총액은 서버가 안 내려줌 - items로 프론트에서 직접 계산해서 씀 (아래 getOrderTotal 참고)
export interface OrderInfo {
  orderId: number;
  email: string;
  addressLine1: string;
  addressLine2: string;
  postalCode: string;
  status: string;
  createdAt: string;
  items: OrderItemInfo[];
}

// OrderInfo.items로 총 금액 계산 (백엔드가 안 주니까 프론트에서 직접 합산)
export function getOrderTotal(order: Pick<OrderInfo, "items">): number {
  return order.items.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0
  );
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
