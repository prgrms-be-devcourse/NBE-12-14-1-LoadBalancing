import { apiClient } from "./client";
import { ApiResponse, PageResponse } from "@/types/common";
import {
  ProductInfo,
  ProductCreateRequest,
  ProductUpdateRequest,
  AdminProductInfo,
} from "@/types/product";

export const productApi = {
  // GET /api/v1/auth/product/list?keyword=...&minPrice=...&maxPrice=...
  // keyword는 빈 값이면 백엔드가 전체 반환. minPrice/maxPrice는 하나만 넣어도 적용됨 (백엔드에서 비어있는 쪽은 0~무제한으로 기본값 처리)
  getList: async (
    page = 0,
    size = 10,
    keyword = "",
    minPrice?: number,
    maxPrice?: number
  ): Promise<PageResponse<ProductInfo>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<ProductInfo>>>(
      "/api/v1/auth/product/list",
      { params: { page, size, keyword: keyword || undefined, minPrice, maxPrice } }
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

  // POST /api/v1/admin/product (예전엔 /api/v1/product였는데, AdminProductController로 옮겨가면서 경로 바뀜)
  create: async (request: ProductCreateRequest): Promise<ProductInfo> => {
    const res = await apiClient.post<ApiResponse<ProductInfo>>(
      "/api/v1/admin/product",
      request
    );
    return res.data.data;
  },

  // PUT /api/v1/admin/product/{productId}
  update: async (
    productId: number,
    request: ProductUpdateRequest
  ): Promise<AdminProductInfo> => {
    const res = await apiClient.put<ApiResponse<AdminProductInfo>>(
      `/api/v1/admin/product/${productId}`,
      request
    );
    return res.data.data;
  },

  // DELETE /api/v1/admin/product/{productId}
  delete: async (productId: number): Promise<void> => {
    await apiClient.delete(`/api/v1/admin/product/${productId}`);
  },

  // PUT /api/v1/admin/product/{productId}/stock
  updateStock: async (productId: number, stock: number): Promise<void> => {
    await apiClient.put(`/api/v1/admin/product/${productId}/stock`, { stock });
  },
};
