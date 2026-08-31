"use client";

import { usePathname, useRouter } from "next/navigation";
import { useCart } from "@/context/CartContext";
import Icon from "@/components/Icon";

export default function CartBar() {
  const router = useRouter();
  const pathname = usePathname();
  const { items, totalCount, totalPrice, updateQuantity, removeItem } =
    useCart();

  // 비어있으면 바 자체를 안 보여줌
  if (totalCount === 0) return null;

  // /order 페이지는 이 하단 바랑 똑같은 정보(상품 목록, 총액, 주문 버튼)를 화면 안에서 이미 보여주고
  // 있어서, 이 바가 화면 아래를 덮어 가리는 문제가 있었음 - 그 페이지에서는 숨김
  if (pathname === "/order") return null;

  // 고객이 장바구니를 담아둔 채로 관리자 페이지로 넘어가면(같은 브라우저/localStorage 공유라
  // 장바구니 상태가 그대로 유지됨) 관리자 화면에 이 바가 계속 떠서 방해가 됨 - 관리자 영역 전체에서 숨김
  if (pathname.startsWith("/admin")) return null;

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
                  disabled={item.quantity >= item.stock}
                  className="flex h-5 w-5 items-center justify-center rounded-full bg-gray-100 text-black disabled:opacity-30"
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

      {/* 총액 + 주문하기 - zz/_3 하단바 타이포 참고 (라벨 위, 큰 총액 아래) */}
      <div className="flex items-center justify-between gap-4 border-t border-gray-100 px-6 py-4">
        <div>
          <p className="text-label-sm uppercase tracking-wide text-gray-400">
            총 금액
          </p>
          <p className="text-headline-md font-extrabold text-black">
            {totalPrice.toLocaleString()}원
          </p>
        </div>
        <span className="text-label-sm text-gray-500">{totalCount}개 담음</span>
        <button
          onClick={() => router.push("/order")}
          className="touch-target flex items-center gap-2 rounded-lg bg-black px-6 text-label-sm font-bold text-white"
        >
          <Icon name="shopping_cart" className="text-lg" />
          주문하기
        </button>
      </div>
    </div>
  );
}
