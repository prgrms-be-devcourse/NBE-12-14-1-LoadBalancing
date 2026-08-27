"use client";

import { useEffect } from "react";
import Link from "next/link";
import { useCart } from "@/context/CartContext";

export default function Home() {
  const { clearCart } = useCart();

  // 처음화면으로 돌아오면 이전에 담아뒀던 장바구니는 초기화
  useEffect(() => {
    clearCart();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="flex h-screen w-screen flex-col items-center justify-center gap-8 bg-black text-white">
      <h1 className="text-3xl font-bold">어서오세요</h1>

      <div className="flex gap-4">
        <Link
          href="/menu"
          className="flex h-40 w-40 flex-col items-center justify-center gap-2 rounded-2xl bg-white text-lg font-bold text-black"
        >
          상품 주문
        </Link>
        <Link
          href="/orders"
          className="flex h-40 w-40 flex-col items-center justify-center gap-2 rounded-2xl bg-yellow-400 text-lg font-bold text-black"
        >
          주문 확인
        </Link>
      </div>
    </div>
  );
}
