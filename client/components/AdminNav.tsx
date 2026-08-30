"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import Icon from "@/components/Icon";
import { useOrderNotifications } from "@/hooks/useOrderNotifications";
import { UNPROCESSED_STATUS_LABEL } from "@/types/notification";

const NAV_ITEMS = [
  { href: "/admin/dashboard", label: "대시보드", icon: "dashboard" },
  { href: "/admin/products", label: "상품 관리", icon: "shopping_cart" },
  { href: "/admin/orders", label: "주문 관리", icon: "receipt_long" },
  { href: "/admin/qna", label: "문의 관리", icon: "help" },
];

export default function AdminNav() {
  const pathname = usePathname();
  const router = useRouter();
  const [notifOpen, setNotifOpen] = useState(false);
  const { notifications, unreadCount, markAsRead, markAllAsRead } =
    useOrderNotifications();

  const handleLogout = () => {
    localStorage.removeItem("admin_token");
    router.push("/admin/login");
  };

  return (
    // 이제 각 페이지 안이 아니라 app/admin/layout.tsx에서 한 번만 렌더링되는 공용 상단바라,
    // 페이지의 w-[896px] 컨테이너 안에 끼어있다고 가정한 -mx-8 bleed 트릭 대신
    // 바 자체를 전체 폭으로 깔고 안쪽 내용만 페이지 폭(896px)에 맞춰 가운데 정렬함
    <div className="mb-8 border-b-2 border-black">
      <div className="mx-auto flex w-[896px] items-center justify-between px-8 py-4">
        <div className="flex items-center gap-10">
        <span className="text-headline-md font-extrabold uppercase tracking-tighter text-black">
          Kiosk
        </span>
        <nav className="flex gap-1">
          {NAV_ITEMS.map((item) => {
            // /admin/products/new, /admin/products/1/edit 같은 하위 경로에서도 "상품 관리" 탭이 활성화되게
            const isActive = pathname.startsWith(item.href);
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`text-label-sm flex items-center gap-1.5 rounded-lg px-3 py-2 font-bold ${
                  isActive
                    ? "bg-black text-white"
                    : "text-gray-500 hover:bg-gray-100"
                }`}
              >
                <Icon name={item.icon} className="text-lg" />
                {item.label}
              </Link>
            );
          })}
        </nav>
      </div>

      <div className="flex items-center gap-4">
        {/* 실시간 주문 알림 - 마운트 시 REST로 히스토리(안 켜져있던 동안 놓친 것 포함)를 먼저 불러오고,
            그 위에 웹소켓으로 실시간 push되는 걸 이어붙임. 읽음 처리는 서버에도 반영됨 */}
        <div className="relative">
          <button
            onClick={() => setNotifOpen((prev) => !prev)}
            className="relative flex h-10 w-10 items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 hover:text-black"
          >
            <Icon name="notifications" className="text-xl" />
            {unreadCount > 0 && (
              <span className="absolute right-1 top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-bold text-white">
                {unreadCount > 9 ? "9+" : unreadCount}
              </span>
            )}
          </button>

          {notifOpen && (
            <div className="absolute right-0 z-20 mt-2 w-80 rounded-lg border border-gray-200 bg-white shadow-lg">
              <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
                <span className="text-label-sm font-bold text-black">
                  주문 알림
                </span>
                {notifications.length > 0 && (
                  <button
                    onClick={markAllAsRead}
                    className="text-label-sm text-gray-400 hover:text-black"
                  >
                    모두 읽음
                  </button>
                )}
              </div>

              <div className="max-h-96 overflow-y-auto">
                {notifications.length === 0 ? (
                  <p className="px-4 py-6 text-center text-sm text-gray-400">
                    새 알림이 없습니다.
                  </p>
                ) : (
                  notifications.map((n) => {
                    // 누군가 읽었는데(readRecords 있음) 주문이 아직 "주문접수" 그대로면 방치된 걸로 봄
                    const isNeglected =
                      n.readRecords.length > 0 &&
                      n.orderStatus === UNPROCESSED_STATUS_LABEL;

                    return (
                      <button
                        key={n.id}
                        onClick={() => {
                          markAsRead(n.id);
                          setNotifOpen(false);
                          router.push("/admin/orders");
                        }}
                        className={`flex w-full flex-col gap-1 border-b border-gray-50 px-4 py-3 text-left hover:bg-gray-50 ${
                          n.readByMe ? "opacity-60" : ""
                        }`}
                      >
                        <div className="flex items-center justify-between gap-2">
                          <span className="text-sm font-bold text-black">
                            주문번호 {n.orderId} · {n.email}
                          </span>
                          <span
                            className={`text-label-sm shrink-0 rounded px-1.5 py-0.5 font-bold ${
                              isNeglected
                                ? "bg-red-50 text-red-600"
                                : "bg-gray-100 text-gray-500"
                            }`}
                          >
                            {n.orderStatus}
                          </span>
                        </div>
                        <span className="text-xs text-gray-500">
                          {new Date(n.createdAt).toLocaleString()}
                        </span>

                        {n.readRecords.length > 0 && (
                          <div className="text-xs text-gray-400">
                            확인:{" "}
                            {n.readRecords
                              .map(
                                (r) =>
                                  `${r.adminId} (${new Date(
                                    r.readAt
                                  ).toLocaleTimeString()})`
                              )
                              .join(", ")}
                          </div>
                        )}
                        {isNeglected && (
                          <span className="text-xs font-bold text-red-600">
                            ⚠ 확인했지만 아직 처리되지 않음
                          </span>
                        )}
                      </button>
                    );
                  })
                )}
              </div>
            </div>
          )}
        </div>

        <button
          onClick={handleLogout}
          className="text-label-sm flex items-center gap-1 font-bold text-gray-400 hover:text-black"
        >
          <Icon name="logout" className="text-lg" />
          로그아웃
        </button>
      </div>
      </div>
    </div>
  );
}
