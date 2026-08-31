// NotificationResponse.ReadRecord (백엔드) - 누가 언제 읽었는지
export interface NotificationReadRecord {
  adminId: string;
  readAt: string;
}

// NotificationResponse.NotificationInfo (백엔드)
// orderStatus는 알림이 온 시점이 아니라 "지금" 주문 상태 - 읽고 방치했는지 판단하는 용도
export interface NotificationInfo {
  id: number;
  orderId: number;
  email: string;
  orderStatus: string;
  createdAt: string;
  readByMe: boolean;
  readRecords: NotificationReadRecord[];
}

// NotificationResponse.NotificationPush (백엔드) - 웹소켓으로 실시간 push되는 페이로드
// notificationId는 백엔드가 DB 저장에 실패했을 때 null로 옴 (그래도 실시간 알림 자체는 보내줌)
export interface NotificationPush {
  notificationId: number | null;
  order: {
    orderId: number;
    email: string;
    status: string;
    createdAt: string;
  };
}

// 주문이 접수된 채로 방치됐다고 볼 상태값들 - 이 상태에서 누군가 읽었는데 안 바뀌었으면 "방치"로 표시
export const UNPROCESSED_STATUS_LABEL = "주문접수";
