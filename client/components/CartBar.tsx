"use client";

import { useRouter } from "next/navigation";
import { useCart } from "@/context/CartContext";
import Icon from "@/components/Icon";

export default function CartBar() {
  const router = useRouter();
  const { items, totalCount, totalPrice, updateQuantity, removeItem } =
    useCart();

  // 비어있으면 바 자체를 안 보여줌
  if (totalCount === 0) return null;

  return (
    <div className="fixed inset-x-0 bottom-0 z-50 border-t-2 border-black bg-white">
      {/* 담은 상품들 - 클릭 없이 항상 보임, 많으면 가로 스크롤 */}
      <div className="flex gap-3 overflow-x-auto px-6 py-3">
        {items.map((item) => (
          <div
            key={item.productId}
            className="flex shrink-0 items-center gap-2 rounded-lg border border-gray-200 p-2 pr-3"
          >
            <div className="h-12 w-12 shrink-0 overflow-hidden rounded-md bg-gray-100">
              {item.thumbnail ? (
                <img
                  src={item.thumbnail}
                  alt={item.title}
                  className="h-full w-full object-cover"
                />
              ) : (
                <div className="flex h-full w-full items-center justify-center">
                  <Icon
                    name="image_not_supported"
                    className="text-lg text-gray-300"
                  />
                </div>
              )}
            </div>

            <div className="flex flex-col gap-1">
              <span className="text-label-sm max-w-[110px] truncate font-bold text-black">
                {item.title}
              </span>
              <div className="flex items-center gap-1.5">
                <button
                  onClick={() =>
                    updateQuantity(item.productId, item.quantity - 1)
                  }
                  className="flex h-5 w-5 items-center justify-center rounded-full bg-gray-100 text-black"
                >
                  <Icon name="remove" className="text-xs" />
                </button>
                <span className="w-4 text-center text-xs text-black">
                  {item.quantity}
                </span>
                <button
                  onClick={() =>
                    updateQuantity(item.productId, item.quantity + 1)
                  }
                  className="flex h-5 w-5 items-center justify-center rounded-full bg-gray-100 text-black"
                >
                  <Icon name="add" className="text-xs" />
                </button>
              </div>
            </div>

            <button
              onClick={() => removeItem(item.productId)}
              className="self-start text-gray-400 hover:text-red-500"
            >
              <Icon name="close" className="text-base" />
            </button>
          </div>
        ))}
      </div>

      {/* 총액 + 주문하기 */}
      <div className="flex items-center justify-between border-t border-gray-100 px-6 py-3">
        <span className="text-body-md text-black">
          {totalCount}개 ·{" "}
          <span className="font-bold">{totalPrice.toLocaleString()}원</span>
        </span>
        <button
          onClick={() => router.push("/order")}
          className="touch-target flex items-center rounded-lg bg-black px-6 text-label-sm font-bold text-white"
        >
          주문하기
        </button>
      </div>
    </div>
  );
}
