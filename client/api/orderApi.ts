import { apiClient } from "./client";
import { ApiResponse, PageResponse } from "@/types/common";
import { OrderCreateRequest, OrderInfo } from "@/types/order";

export const orderApi = {
  // POST /api/v1/auth/order
  create: async (request: OrderCreateRequest): Promise<OrderInfo> => {
    const res = await apiClient.post<ApiResponse<OrderInfo>>(
      "/api/v1/auth/order",
      request
    );
    return res.data.data;
  },

  // GET /api/v1/admin/order/search (파라미터 없이 호출) - 관리자용 전체 주문 목록 (email 필터 없음)
  // 예전엔 /order/admin/list가 따로 있었는데, search가 파라미터 없으면 전체 목록과 동일해서 그쪽으로 흡수됨
  getList: async (
    page = 0,
    size = 10
  ): Promise<PageResponse<OrderInfo>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<OrderInfo>>>(
      "/api/v1/admin/order/search",
      { params: { page, size } }
    );
    return res.data.data;
  },

  // GET /api/v1/auth/order/list?email=... - 고객이 자기 이메일로 주문 조회
  getMyList: async (
    email: string,
    page = 0,
    size = 10
  ): Promise<PageResponse<OrderInfo>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<OrderInfo>>>(
      "/api/v1/auth/order/list",
      { params: { email, page, size } }
    );
    return res.data.data;
  },

  // GET /api/v1/admin/order/search?keyword=&status=&startDate=&endDate= - 관리자용 주문 검색
  // keyword: 이메일 기준 검색, status: OrderStatus enum 이름(빈 값이면 전체), startDate/endDate: yyyy-MM-dd 문자열 (없으면 전체 기간)
  search: async (
    keyword: string,
    status: string,
    startDate: string,
    endDate: string,
    page = 0,
    size = 10
  ): Promise<PageResponse<OrderInfo>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<OrderInfo>>>(
      "/api/v1/admin/order/search",
      {
        params: {
          keyword: keyword || undefined,
          status: status || undefined,
          startDate: startDate || undefined,
          endDate: endDate || undefined,
          page,
          size,
        },
      }
    );
    return res.data.data;
  },

  // PATCH /api/v1/admin/order/{id}/status - status는 OrderStatus enum 이름 그대로 보내야 함
  updateStatus: async (orderId: number, status: string): Promise<void> => {
    await apiClient.patch(`/api/v1/admin/order/${orderId}/status`, { status });
  },

  // DELETE /api/v1/admin/order/{id}
  delete: async (orderId: number): Promise<void> => {
    await apiClient.delete(`/api/v1/admin/order/${orderId}`);
  },

  // DELETE /api/v1/auth/order/{orderId}/items/{itemId} - 고객이 자기 주문에서 항목 하나만 뺄 때 사용
  // (로그인 없이 되는 공개 API라, 주문 상세를 아는 사람이면 누구나 지울 수 있음 - 지금은 이메일 조회 흐름 뒤에서만 노출)
  deleteItem: async (orderId: number, itemId: number): Promise<void> => {
    await apiClient.delete(`/api/v1/auth/order/${orderId}/items/${itemId}`);
  },
};
