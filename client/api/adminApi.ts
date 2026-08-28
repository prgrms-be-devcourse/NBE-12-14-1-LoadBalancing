import { apiClient } from "./client";
import { ApiResponse } from "@/types/common";
import { LoginRequest, LoginResponse, AdminDashboardResponse } from "@/types/admin";

export const adminApi = {
  // POST /api/v1/auth/admin/login
  login: async (request: LoginRequest): Promise<LoginResponse> => {
    const res = await apiClient.post<ApiResponse<LoginResponse>>(
      "/api/v1/auth/admin/login",
      request
    );
    return res.data.data;
  },

  // GET /api/v1/admin/dashboard - 아직 상품 통계만 있는 초기 버전
  getDashboard: async (): Promise<AdminDashboardResponse> => {
    const res = await apiClient.get<ApiResponse<AdminDashboardResponse>>(
      "/api/v1/admin/dashboard"
    );
    return res.data.data;
  },
};
