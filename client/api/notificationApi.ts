import { apiClient } from "./client";
import { ApiResponse, PageResponse } from "@/types/common";
import { NotificationInfo } from "@/types/notification";

export const notificationApi = {
  // GET /api/v1/admin/notifications - 로그인한 관리자 기준으로 readByMe/readRecords가 채워져서 옴
  getList: async (
    page = 0,
    size = 20
  ): Promise<PageResponse<NotificationInfo>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<NotificationInfo>>>(
      "/api/v1/admin/notifications",
      { params: { page, size } }
    );
    return res.data.data;
  },

  // GET /api/v1/admin/notifications/unread-count
  getUnreadCount: async (): Promise<number> => {
    const res = await apiClient.get<ApiResponse<{ count: number }>>(
      "/api/v1/admin/notifications/unread-count"
    );
    return res.data.data.count;
  },

  // PATCH /api/v1/admin/notifications/{id}/read
  markRead: async (id: number): Promise<void> => {
    await apiClient.patch(`/api/v1/admin/notifications/${id}/read`);
  },

  // PATCH /api/v1/admin/notifications/read-all
  markAllRead: async (): Promise<void> => {
    await apiClient.patch("/api/v1/admin/notifications/read-all");
  },
};
