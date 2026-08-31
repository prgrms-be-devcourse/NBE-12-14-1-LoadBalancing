"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import { notificationApi } from "@/api/notificationApi";
import { NotificationInfo, NotificationPush } from "@/types/notification";

// NEXT_PUBLIC_API_BASE_URL(http/https)을 ws/wss로 바꿔서 STOMP 브로커 주소를 만듦
function resolveBrokerUrl(): string {
  const base =
    process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
  const wsBase = base.replace(/^http/, "ws");
  return `${wsBase}/ws`;
}

// 관리자 실시간 주문 알림 훅.
// - 마운트 시점에 REST(GET /notifications)로 그동안 쌓인 알림 히스토리를 먼저 불러오고,
//   그 위에 웹소켓으로 실시간 push되는 걸 이어붙임 -> "로그인 안 하고 있던 동안 놓친 것"까지 다 보임
// - 읽음 처리는 로컬 state만 바꾸는 게 아니라 서버(/read, /read-all)에도 반영해서
//   새로고침해도, 다른 세션에서 봐도 유지됨
export function useOrderNotifications() {
  const [notifications, setNotifications] = useState<NotificationInfo[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);

  const loadHistory = useCallback(async () => {
    try {
      const [page, count] = await Promise.all([
        notificationApi.getList(0, 20),
        notificationApi.getUnreadCount(),
      ]);
      setNotifications(page.content);
      setUnreadCount(count);
    } catch (e) {
      console.error("[알림 히스토리 조회 실패]", e);
    }
  }, []);

  useEffect(() => {
    const token =
      typeof window !== "undefined"
        ? localStorage.getItem("admin_token")
        : null;

    // 로그인 안 된 상태(토큰 없음)면 히스토리 조회도, 웹소켓 연결도 시도 안 함
    if (!token) {
      return;
    }

    loadHistory();

    const client = new Client({
      brokerURL: resolveBrokerUrl(),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000, // 연결 끊기면 5초마다 재시도
      onConnect: () => {
        setConnected(true);
        client.subscribe("/topic/admin/orders", (message: IMessage) => {
          const push = JSON.parse(message.body) as NotificationPush;

          // 백엔드가 DB 저장에 실패하면 notificationId가 null로 옴(그래도 실시간 알림은 보내줌).
          // 그런 경우 화면에 보여줄 임시 id를 음수로 만들어 씀 - 진짜 DB id(양수, 1부터 시작)랑
          // 절대 안 겹치고, markAsRead 쪽에서 "음수면 서버에 읽음 처리 API 호출 안 함"으로 구분함
          const notificationId = push.notificationId ?? -Date.now();

          setNotifications((prev) => {
            // 히스토리 조회 직후에 새 주문이 들어오면 REST/웹소켓 둘 다로 잡힐 수 있어서 id로 중복 제거
            if (prev.some((n) => n.id === notificationId)) {
              return prev;
            }
            const next: NotificationInfo = {
              id: notificationId,
              orderId: push.order.orderId,
              email: push.order.email,
              orderStatus: push.order.status,
              createdAt: push.order.createdAt,
              readByMe: false,
              readRecords: [],
            };
            return [next, ...prev];
          });
          setUnreadCount((prev) => prev + 1);
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const markAsRead = useCallback(async (id: number) => {
    // 이미 읽은 걸 또 누르면 그냥 무시 (서버도 중복 방지하지만, 언읽음 카운트가 잘못 줄어드는 것도 막음)
    let wasUnread = false;
    setNotifications((prev) =>
      prev.map((n) => {
        if (n.id === id && !n.readByMe) {
          wasUnread = true;
          return { ...n, readByMe: true };
        }
        return n;
      })
    );
    if (!wasUnread) return;

    setUnreadCount((prev) => Math.max(0, prev - 1));

    // 저장 실패했던 알림(음수 임시 id)은 서버에 실제 행이 없어서 읽음 처리 API를 호출할 대상이 없음
    if (id <= 0) return;

    try {
      await notificationApi.markRead(id);
    } catch (e) {
      console.error("[알림 읽음 처리 실패]", e);
    }
  }, []);

  const markAllAsRead = useCallback(async () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, readByMe: true })));
    setUnreadCount(0);

    try {
      await notificationApi.markAllRead();
    } catch (e) {
      console.error("[알림 전체 읽음 처리 실패]", e);
    }
  }, []);

  return {
    notifications,
    unreadCount,
    connected,
    markAsRead,
    markAllAsRead,
    reload: loadHistory,
  };
}
