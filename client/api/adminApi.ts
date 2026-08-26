import { apiClient } from "./client";
import { ApiResponse } from "@/types/common";
import { LoginRequest, LoginResponse } from "@/types/admin";

export const adminApi = {
  // POST /api/v1/auth/admin/login
  login: async (request: LoginRequest): Promise<LoginResponse> => {
    const res = await apiClient.post<ApiResponse<LoginResponse>>(
      "/api/v1/auth/admin/login",
      request
    );
    return res.data.data;
  },
};
