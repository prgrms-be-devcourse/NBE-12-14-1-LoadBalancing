"use client";

import { useCallback, useState } from "react";
import { orderApi } from "@/api/orderApi";
import { OrderInfo } from "@/types/order";
import BackToHomeButton from "@/components/BackToHomeButton";
import Icon from "@/components/Icon";

const PAGE_SIZE = 10;

type Phase = "email" | "list";

export default function CustomerOrderLookupPage() {
  const [phase, setPhase] = useState<Phase>("email");
  const [email, setEmail] = useState("");

  const [orders, setOrders] = useState<OrderInfo[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<{ code: number; message: string } | null>(
    null
  );

  // 지금 펼쳐져 있는 주문 id들
  const [expandedIds, setExpandedIds] = useState<Set<number>>(new Set());

  const fetchPage = useCallback(
    async (targetPage: number, targetEmail: string) => {
      setLoading(true);
      setError(null);

      try {
        const res = await orderApi.getMyList(
          targetEmail,
          targetPage,
          PAGE_SIZE
        );
        console.log("주문 목록 응답:", res); // 확인용, 나중에 지워도 됨
        setOrders(res.content);
        setTotalPages(res.totalPages);
        setPage(res.number);
      } catch (e) {
        const axiosError = e as {
          response?: { data?: { code?: number; message?: string } };
        };
        setError({
          code: axiosError.response?.data?.code ?? 500,
          message:
            axiosError.response?.data?.message ??
            "알 수 없는 오류가 발생했습니다.",
        });
      } finally {
        setLoading(false);
      }
    },
    []
  );

  const handleEmailSubmit = () => {
    if (!email) {
      setError({ code: 400, message: "이메일을 입력해주세요." });
      return;
    }
    setPhase("list");
    fetchPage(0, email);
  };

  const toggleExpand = (orderId: number) => {
    setExpandedIds((prev) => {
      const next = new Set(prev);
      if (next.has(orderId)) {
        next.delete(orderId);
      } else {
        next.add(orderId);
      }
      return next;
    });
  };

  // 1단계: 이메일 입력 (로그인 창처럼)
  if (phase === "email") {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-6 px-8">
        <BackToHomeButton />
        <Icon name="receipt_long" className="text-5xl text-black" />
        <h1 className="text-headline-md font-bold text-black">
          이메일을 입력해주세요
        </h1>

        <div className="flex w-full max-w-sm flex-col gap-4">
          <input
            type="email"
            placeholder="주문할 때 입력한 이메일"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") handleEmailSubmit();
            }}
            className="text-body-md rounded-lg border border-gray-200 px-4 py-3 text-black"
          />

          {error && (
            <div className="rounded-lg bg-red-50 p-4 text-sm text-red-600">
              [{error.code}] {error.message}
            </div>
          )}

          <button
            onClick={handleEmailSubmit}
            className="touch-target rounded-lg bg-black font-bold text-white"
          >
            조회하기
          </button>
        </div>
      </div>
    );
  }

  // 2단계: 주문 목록 (admin/orders랑 같은 템플릿, 상태변경/삭제는 뺌 - 고객은 조회만)
  return (
    <div className="mx-auto max-w-2xl px-8 py-10">
      <BackToHomeButton />
      <h1 className="text-headline-md mb-8 font-bold text-black">
        주문 조회
      </h1>

      {error && (
        <div className="mb-6 flex items-center justify-between rounded-lg bg-red-50 p-4 text-sm text-red-600">
          <span>
            [{error.code}] {error.message}
          </span>
          <button
            onClick={() => fetchPage(page, email)}
            className="ml-4 rounded bg-red-100 px-3 py-1 font-medium hover:bg-red-200"
          >
            다시 시도
          </button>
        </div>
      )}

      {loading && (
        <div className="flex justify-center py-16">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-gray-200 border-t-gray-800" />
        </div>
      )}

      {!loading && !error && orders.length === 0 && (
        <p className="text-gray-500">주문하신 목록이 없습니다.</p>
      )}

      {!loading && orders.length > 0 && (
        <>
          <div className="flex flex-col gap-3">
            {orders.map((order) => {
              const isExpanded = expandedIds.has(order.orderId);
              return (
                <div
                  key={order.orderId}
                  className="rounded-lg border border-gray-200"
                >
                  {/* 주문(order) 한 줄 - 클릭하면 아래로 orderItem들이 펼쳐짐 */}
                  <button
                    onClick={() => toggleExpand(order.orderId)}
                    className="flex w-full items-center justify-between px-4 py-3 text-left"
                  >
                    <div>
                      <p className="font-medium text-black">
                        주문번호 {order.orderId}
                      </p>
                      <p className="text-xs text-gray-500">
                        {new Date(order.createdAt).toLocaleString()} · 상품{" "}
                        {order.items.length}종
                      </p>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="text-sm text-black">
                        {order.status}
                      </span>
                      <Icon
                        name={isExpanded ? "expand_less" : "expand_more"}
                        className="text-gray-400"
                      />
                    </div>
                  </button>

                  {/* 펼쳐짐/접힘을 grid-template-rows로 부드럽게 애니메이션 (박스 크기가 갑자기 툭 바뀌는 것 방지) */}
                  <div
                    className={`grid transition-[grid-template-rows] duration-300 ease-in-out ${
                      isExpanded ? "grid-rows-[1fr]" : "grid-rows-[0fr]"
                    }`}
                  >
                    <div className="overflow-hidden">
                      <div className="border-t border-gray-100 px-4 py-3">
                        <div className="mb-3 flex flex-col gap-2">
                          {order.items.map((item, index) => (
                            <div
                              // productId만 쓰면 한 주문 안에 같은 상품이 두 줄로 들어올 때(예: 따로 담아서
                              // 합쳐지지 않은 경우) key가 중복돼서 index를 같이 섞어 고유하게 만듦
                              key={`${item.productId}-${index}`}
                              className="flex justify-between text-sm text-black"
                            >
                              <span>
                                {item.title} x {item.quantity}
                              </span>
                              <span>
                                {(item.price * item.quantity).toLocaleString()}
                                원
                              </span>
                            </div>
                          ))}
                        </div>
                        <p className="text-xs text-gray-500">
                          배송지: {order.addressLine1} {order.addressLine2} (
                          {order.postalCode})
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* 페이지 이동 */}
          <div className="mt-6 flex items-center justify-center gap-4">
            <button
              onClick={() => fetchPage(page - 1, email)}
              disabled={page === 0}
              className="rounded border border-gray-200 px-3 py-1 text-black disabled:opacity-30"
            >
              이전
            </button>
            <span className="text-sm text-gray-500">
              {page + 1} / {totalPages}
            </span>
            <button
              onClick={() => fetchPage(page + 1, email)}
              disabled={page + 1 >= totalPages}
              className="rounded border border-gray-200 px-3 py-1 text-black disabled:opacity-30"
            >
              다음
            </button>
          </div>
        </>
      )}
    </div>
  );
}
