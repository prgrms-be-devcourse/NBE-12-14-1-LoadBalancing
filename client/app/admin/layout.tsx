"use client";

import { useEffect, useState } from "react";
import { usePathname, useRouter } from "next/navigation";
import AdminNav from "@/components/AdminNav";

// 관리자 페이지 2차 방어(프론트 라우트 가드).
// 진짜 방어는 백엔드 SecurityConfig가 하지만(토큰 없으면 API가 401), 그것만 믿으면
// 로그인 안 한 사람도 관리자 페이지 화면 자체(레이아웃, 빈 목록 등)는 열어볼 수 있어서
// 여기서 한 번 더 "토큰 없으면 아예 로그인 페이지로" 막아준다.
// 토큰은 localStorage에만 있어서(쿠키 아님) middleware.ts(서버/엣지)로는 못 읽고, 클라이언트 레이아웃에서 체크함.
export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const router = useRouter();
  // 로그인 페이지 자체는 이 레이아웃 아래 있지만 가드 대상이 아님(안 그러면 리다이렉트 루프)
  const isLoginPage = pathname === "/admin/login";
  const [checked, setChecked] = useState(false);

  useEffect(() => {
    if (isLoginPage) {
      setChecked(true);
      return;
    }

    const token = localStorage.getItem("admin_token");
    if (!token) {
      router.replace("/admin/login");
      return;
    }

    setChecked(true);
  }, [isLoginPage, pathname, router]);

  // 확인 끝나기 전엔 아무것도 안 그려서, 로그인 안 된 상태로 관리자 화면이 잠깐이라도 보이는 걸 막음
  if (!isLoginPage && !checked) {
    return null;
  }

  // AdminNav(+ 알림 벨/웹소켓)를 페이지마다 따로 안 두고 레이아웃 하나에 둠 - 예전엔 페이지 이동할
  // 때마다 AdminNav가 통째로 마운트/언마운트되면서 웹소켓도 매번 끊겼다 재연결됐는데, 여기 두면
  // /admin/* 안에서 페이지를 옮겨다녀도 연결이 안 끊기고 유지됨
  return (
    <>
      {!isLoginPage && <AdminNav />}
      {children}
    </>
  );
}
