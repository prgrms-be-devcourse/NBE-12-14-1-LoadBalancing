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
