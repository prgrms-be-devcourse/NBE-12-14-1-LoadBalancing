import { apiClient } from "./client";
import { ApiResponse } from "@/types/common";

export const imageApi = {
  // POST /api/v1/admin/images - 파일 하나 업로드하고 저장된 URL을 받음
  upload: async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append("file", file);

    const res = await apiClient.post<ApiResponse<{ url: string }>>(
      "/api/v1/admin/images",
      formData,
      { headers: { "Content-Type": "multipart/form-data" } }
    );
    return res.data.data.url;
  },
};
