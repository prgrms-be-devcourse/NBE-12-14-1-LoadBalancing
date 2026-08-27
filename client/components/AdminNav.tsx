"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import Icon from "@/components/Icon";

const NAV_ITEMS = [
  { href: "/admin/dashboard", label: "대시보드", icon: "dashboard" },
  { href: "/admin/products", label: "상품 관리", icon: "shopping_cart" },
  { href: "/admin/orders", label: "주문 관리", icon: "receipt_long" },
];

export default function AdminNav() {
  const pathname = usePathname();
  const router = useRouter();

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
      <button
        onClick={handleLogout}
        className="text-label-sm flex items-center gap-1 font-bold text-gray-400 hover:text-black"
      >
        <Icon name="logout" className="text-lg" />
        로그아웃
      </button>
    </div>
  );
}
