import { apiClient } from "./client";
import { ApiResponse } from "@/types/common";
import { OrderCreateRequest, OrderCreateResponse } from "@/types/order";

export const orderApi = {
  // POST /api/v1/order
  create: async (request: OrderCreateRequest): Promise<OrderCreateResponse> => {
    const res = await apiClient.post<ApiResponse<OrderCreateResponse>>(
      "/api/v1/order",
      request
    );
    return res.data.data;
  },
};
