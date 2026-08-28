"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import { OrderInfo } from "@/types/order";

// 최근 알림 몇 개까지만 화면에 들고 있을지 (저장소가 없어서 그냥 인메모리 배열, 새로고침하면 날아감)
const MAX_NOTIFICATIONS = 20;

export interface OrderNotification {
  // 웹소켓 메시지 자체엔 별도 id가 없어서, 받은 시점 타임스탬프+orderId로 프론트에서 조합해서 씀
  id: string;
  order: OrderInfo;
  receivedAt: string;
  read: boolean;
}

// NEXT_PUBLIC_API_BASE_URL(http/https)을 ws/wss로 바꿔서 STOMP 브로커 주소를 만듦
function resolveBrokerUrl(): string {
  const base =
    process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
  const wsBase = base.replace(/^http/, "ws");
  return `${wsBase}/ws`;
}

// 관리자 실시간 주문 알림 훅.
// - /topic/admin/orders 구독, 새 주문 오면 최근 N개까지만 인메모리로 들고 있음 (백엔드에 저장 안 되므로
//   새로고침하면 사라짐 - 지금 접속해있는 동안 실시간으로 뭐 왔는지 확인하는 용도)
// - CONNECT 프레임 헤더에 admin_token을 실어 보내야 백엔드 StompAuthChannelInterceptor를 통과함
export function useOrderNotifications() {
  const [notifications, setNotifications] = useState<OrderNotification[]>([]);
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    const token =
      typeof window !== "undefined"
        ? localStorage.getItem("admin_token")
        : null;

    // 로그인 안 된 상태(토큰 없음)면 애초에 연결 시도 안 함
    if (!token) {
      return;
    }

    const client = new Client({
      brokerURL: resolveBrokerUrl(),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000, // 연결 끊기면 5초마다 재시도
      onConnect: () => {
        setConnected(true);
        client.subscribe("/topic/admin/orders", (message: IMessage) => {
          const order = JSON.parse(message.body) as OrderInfo;
          setNotifications((prev) => {
            const next: OrderNotification[] = [
              {
                id: `${order.orderId}-${Date.now()}`,
                order,
                receivedAt: new Date().toISOString(),
                read: false,
              },
              ...prev,
            ];
            return next.slice(0, MAX_NOTIFICATIONS);
          });
        });
      },
      onDisconnect: () => setConnected(false),
      onWebSocketClose: () => setConnected(false),
      // 인증 실패(토큰 없음/무효)면 서버가 CONNECT를 거부하면서 STOMP ERROR 프레임을 보냄
      onStompError: (frame) => {
        console.error("[웹소켓 STOMP 에러]", frame.headers["message"]);
        setConnected(false);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, []);

  const markAsRead = useCallback((id: string) => {
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, read: true } : n))
    );
  }, []);

  const markAllAsRead = useCallback(() => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
  }, []);

  const unreadCount = notifications.filter((n) => !n.read).length;

  return { notifications, unreadCount, connected, markAsRead, markAllAsRead };
}
