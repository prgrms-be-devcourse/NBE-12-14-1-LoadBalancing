import { apiClient } from "./client";
import { ApiResponse, PageResponse } from "@/types/common";
import { ProductInfo } from "@/types/product";

export const productApi = {
  // GET /api/v1/auth/product/list
  getList: async (page = 0, size = 10): Promise<PageResponse<ProductInfo>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<ProductInfo>>>(
      "/api/v1/auth/product/list",
      { params: { page, size } }
    );
    return res.data.data;
  },

  // GET /api/v1/auth/product/detail/{id}
  getDetail: async (id: number): Promise<ProductInfo> => {
    const res = await apiClient.get<ApiResponse<ProductInfo>>(
      `/api/v1/auth/product/detail/${id}`
    );
    return res.data.data;
  },
};
