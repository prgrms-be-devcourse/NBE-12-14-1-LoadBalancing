"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import Icon from "@/components/Icon";
import { useOrderNotifications } from "@/hooks/useOrderNotifications";

const NAV_ITEMS = [
  { href: "/admin/dashboard", label: "대시보드", icon: "dashboard" },
  { href: "/admin/products", label: "상품 관리", icon: "shopping_cart" },
  { href: "/admin/orders", label: "주문 관리", icon: "receipt_long" },
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
    <div className="-mx-8 mb-8 flex items-center justify-between border-b-2 border-black px-8 py-4">
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
        {/* 실시간 주문 알림 - 백엔드가 저장 안 하고 그때그때 push만 하는 구조라, 접속해있는 동안
            받은 것만 인메모리로 보여줌 (새로고침하면 비워짐) */}
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
                  notifications.map((n) => (
                    <button
                      key={n.id}
                      onClick={() => {
                        markAsRead(n.id);
                        setNotifOpen(false);
                        router.push("/admin/orders");
                      }}
                      className={`flex w-full flex-col gap-0.5 border-b border-gray-50 px-4 py-3 text-left hover:bg-gray-50 ${
                        n.read ? "opacity-50" : ""
                      }`}
                    >
                      <span className="text-sm font-bold text-black">
                        주문번호 {n.order.orderId} · {n.order.email}
                      </span>
                      <span className="text-xs text-gray-500">
                        {new Date(n.receivedAt).toLocaleTimeString()} 접수
                      </span>
                    </button>
                  ))
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
  );
}
