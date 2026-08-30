import { apiClient } from "./client";
import { ApiResponse, PageResponse } from "@/types/common";
import { QnaAnswerRequest, QnaCreateRequest, QnaInfo } from "@/types/qna";

export const qnaApi = {
  // POST /api/v1/auth/qna
  create: async (request: QnaCreateRequest): Promise<QnaInfo> => {
    const res = await apiClient.post<ApiResponse<QnaInfo>>(
      "/api/v1/auth/qna",
      request
    );
    return res.data.data;
  },

  // GET /api/v1/auth/qna - email 없이 호출하면 전체 목록(관리자용), email과 함께 호출하면 그 이메일 것만(고객용)
  getList: async (
    page = 0,
    size = 10,
    email?: string
  ): Promise<PageResponse<QnaInfo>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<QnaInfo>>>(
      "/api/v1/auth/qna",
      { params: { page, size, email } }
    );
    return res.data.data;
  },

  // GET /api/v1/auth/qna/{id}
  getDetail: async (id: number): Promise<QnaInfo> => {
    const res = await apiClient.get<ApiResponse<QnaInfo>>(
      `/api/v1/auth/qna/${id}`
    );
    return res.data.data;
  },

  // PATCH /api/v1/auth/qna/{id}/answer
  // 원래 관리자 전용 액션인데 백엔드가 아직 /auth(공개) 경로 밑에 두고 있음 - 알려진 이슈,
  // 관리자 화면에서만 노출해서 쓰고 있고 백엔드 경로 자체는 이번 작업 범위 밖이라 그대로 둠
  answer: async (id: number, request: QnaAnswerRequest): Promise<QnaInfo> => {
    const res = await apiClient.patch<ApiResponse<QnaInfo>>(
      `/api/v1/auth/qna/${id}/answer`,
      request
    );
    return res.data.data;
  },
};
