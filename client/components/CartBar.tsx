"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useCart } from "@/context/CartContext";

export default function CartBar() {
  const router = useRouter();
  const { items, totalCount, totalPrice, updateQuantity, removeItem } =
    useCart();
  const [open, setOpen] = useState(false);

  // 비어있으면 바 자체를 안 보여줌
  if (totalCount === 0) return null;

  return (
    <>
      {/* 하단바 누르면 그 위로 팝업되는 장바구니 목록 */}
      {open && (
        <div className="fixed inset-x-0 bottom-16 z-40 mx-auto max-h-[60vh] w-full max-w-3xl overflow-y-auto rounded-t-2xl bg-white p-6 shadow-2xl">
          <h2 className="mb-4 text-lg font-bold text-black">장바구니</h2>

          <div className="flex flex-col gap-4">
            {items.map((item) => (
              <div
                key={item.productId}
                className="flex items-center justify-between"
              >
                <div>
                  <p className="font-medium text-black">{item.title}</p>
                  <p className="text-sm text-gray-500">
                    {item.price.toLocaleString()}원
                  </p>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() =>
                      updateQuantity(item.productId, item.quantity - 1)
                    }
                    className="h-7 w-7 rounded-full bg-gray-100 text-black"
                  >
                    -
                  </button>
                  <span className="w-6 text-center text-black">
                    {item.quantity}
                  </span>
                  <button
                    onClick={() =>
                      updateQuantity(item.productId, item.quantity + 1)
                    }
                    className="h-7 w-7 rounded-full bg-gray-100 text-black"
                  >
                    +
                  </button>
                  <button
                    onClick={() => removeItem(item.productId)}
                    className="ml-2 text-sm text-red-500"
                  >
                    삭제
                  </button>
                </div>
              </div>
            ))}
          </div>

          <button
            onClick={() => router.push("/order")}
            className="mt-6 w-full rounded-lg bg-black py-3 font-medium text-white"
          >
            주문하기
          </button>
        </div>
      )}

      {/* 화면 하단에 고정되는 바 */}
      <button
        onClick={() => setOpen((prev) => !prev)}
        className="fixed inset-x-0 bottom-0 z-50 flex items-center justify-between bg-black px-8 py-4 text-white"
      >
        <span>{totalCount}개</span>
        <span className="font-bold">{totalPrice.toLocaleString()}원</span>
        <span>{open ? "닫기" : "장바구니 보기"}</span>
      </button>
    </>
  );
}
