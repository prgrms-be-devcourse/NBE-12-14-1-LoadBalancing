import { apiClient } from "./client";
import { ApiResponse, PageResponse } from "@/types/common";
import {
  ProductInfo,
  ProductCreateRequest,
  ProductUpdateRequest,
  AdminProductInfo,
} from "@/types/product";

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

  // POST /api/v1/product
  create: async (request: ProductCreateRequest): Promise<ProductInfo> => {
    const res = await apiClient.post<ApiResponse<ProductInfo>>(
      "/api/v1/product",
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
