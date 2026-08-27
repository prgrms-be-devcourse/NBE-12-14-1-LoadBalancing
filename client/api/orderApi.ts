import { apiClient } from "./client";
import { ApiResponse, PageResponse } from "@/types/common";
import {
  OrderCreateRequest,
  OrderCreateResponse,
  OrderListItem,
} from "@/types/order";

export const orderApi = {
  // POST /api/v1/order
  create: async (request: OrderCreateRequest): Promise<OrderCreateResponse> => {
    const res = await apiClient.post<ApiResponse<OrderCreateResponse>>(
      "/api/v1/order",
      request
    );
    return res.data.data;
  },

  // GET /api/v1/order/admin/list - 관리자용 전체 주문 목록 (email 필터 없음)
  getList: async (
    page = 0,
    size = 10
  ): Promise<PageResponse<OrderListItem>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<OrderListItem>>>(
      "/api/v1/order/admin/list",
      { params: { page, size } }
    );
    return res.data.data;
  },

  // PATCH /api/v1/admin/order/status/{id} - status는 OrderStatus enum 이름 그대로 보내야 함
  updateStatus: async (orderId: number, status: string): Promise<void> => {
    await apiClient.patch(`/api/v1/admin/order/status/${orderId}`, { status });
  },

  // DELETE /api/v1/admin/order/{id}
  delete: async (orderId: number): Promise<void> => {
    await apiClient.delete(`/api/v1/admin/order/${orderId}`);
  },
};
