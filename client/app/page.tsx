"use client";

import { useEffect } from "react";
import Link from "next/link";
import { useCart } from "@/context/CartContext";
import Icon from "@/components/Icon";

export default function Home() {
  const { clearCart } = useCart();

  // 처음화면으로 돌아오면 이전에 담아뒀던 장바구니는 초기화
  useEffect(() => {
    clearCart();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="flex h-screen w-screen flex-col bg-white text-black">
      <header className="flex h-24 shrink-0 items-center justify-center border-b-2 border-black">
        <span className="text-headline-md font-extrabold uppercase tracking-tighter">
          Kiosk
        </span>
      </header>

      <main className="flex flex-1 flex-col items-center justify-center gap-10 px-8">
        <div className="text-center">
          <h1 className="text-headline-lg font-bold">어서오세요</h1>
          <p className="text-body-md mt-2 text-gray-500">
            원하시는 메뉴를 선택해주세요
          </p>
        </div>

        <div className="grid w-full max-w-2xl grid-cols-1 gap-6 sm:grid-cols-2">
          <Link
            href="/menu"
            className="touch-target flex min-h-[240px] flex-col items-center justify-center gap-3 rounded-xl border-4 border-transparent bg-black text-white transition-colors hover:bg-black/90"
          >
            <Icon name="restaurant_menu" className="text-6xl" filled />
            <span className="text-headline-md font-bold">상품 주문</span>
          </Link>
          <Link
            href="/orders"
            className="touch-target flex min-h-[240px] flex-col items-center justify-center gap-3 rounded-xl border-4 border-black bg-white text-black transition-colors hover:bg-black hover:text-white"
          >
            <Icon name="receipt_long" className="text-6xl" />
            <span className="text-headline-md font-bold">주문 확인</span>
          </Link>
        </div>

        {/* 메인 2버튼 디자인은 그대로 두고, 아래 보조 링크로만 문의하기 노출 */}
        <Link
          href="/qna"
          className="flex items-center gap-1 text-body-md font-medium text-gray-400 hover:text-black"
        >
          <Icon name="help" className="text-lg" />
          문의하기
        </Link>
      </main>
    </div>
  );
}
