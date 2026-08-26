"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useCart } from "@/context/CartContext";
import { orderApi } from "@/api/orderApi";

export default function OrderPage() {
  const router = useRouter();
  const { items, totalPrice, clearCart } = useCart();

  const [email, setEmail] = useState("");
  const [addressLine1, setAddressLine1] = useState("");
  const [addressLine2, setAddressLine2] = useState("");
  const [postalCode, setPostalCode] = useState("");

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<{ code: number; message: string } | null>(
    null
  );
  const [done, setDone] = useState(false);

  const handleSubmit = async () => {
    if (submitting) return;
    setSubmitting(true);
    setError(null);

    try {
      await orderApi.create({
        email,
        addressLine1,
        addressLine2,
        postalCode,
        items: items.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
        })),
      });
      clearCart();
      setDone(true);
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
      setSubmitting(false);
    }
  };

  // 주문 완료 화면
  if (done) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-4">
        <p className="text-xl font-bold text-black">주문이 완료되었습니다</p>
        <button
          onClick={() => router.push("/menu")}
          className="rounded-lg bg-black px-6 py-3 text-white"
        >
          메뉴로 돌아가기
        </button>
      </div>
    );
  }

  // 장바구니가 비어있는데 이 페이지로 바로 들어온 경우
  if (items.length === 0) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-4">
        <p className="text-gray-500">장바구니가 비어있습니다.</p>
        <button
          onClick={() => router.push("/menu")}
          className="rounded-lg bg-black px-6 py-3 text-white"
        >
          메뉴 보러가기
        </button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-2xl px-8 py-10 pb-32">
      <h1 className="mb-8 text-2xl font-bold text-black">주문하기</h1>

      {/* 담은 상품 요약 */}
      <div className="mb-8 flex flex-col gap-3 rounded-lg bg-gray-50 p-4">
        {items.map((item) => (
          <div
            key={item.productId}
            className="flex justify-between text-sm text-black"
          >
            <span>
              {item.title} x {item.quantity}
            </span>
            <span>{(item.price * item.quantity).toLocaleString()}원</span>
          </div>
        ))}
        <div className="mt-2 flex justify-between border-t border-gray-200 pt-2 font-bold text-black">
          <span>총 금액</span>
          <span>{totalPrice.toLocaleString()}원</span>
        </div>
      </div>

      {/* 배송 정보 입력 폼 */}
      <div className="flex flex-col gap-4">
        <input
          type="email"
          placeholder="이메일"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="rounded-lg border border-gray-200 px-4 py-3 text-black"
        />
        <input
          type="text"
          placeholder="주소"
          value={addressLine1}
          onChange={(e) => setAddressLine1(e.target.value)}
          className="rounded-lg border border-gray-200 px-4 py-3 text-black"
        />
        <input
          type="text"
          placeholder="상세주소"
          value={addressLine2}
          onChange={(e) => setAddressLine2(e.target.value)}
          className="rounded-lg border border-gray-200 px-4 py-3 text-black"
        />
        <input
          type="text"
          placeholder="우편번호"
          value={postalCode}
          onChange={(e) => setPostalCode(e.target.value)}
          className="rounded-lg border border-gray-200 px-4 py-3 text-black"
        />
      </div>

      {error && (
        <div className="mt-4 rounded-lg bg-red-50 p-4 text-sm text-red-600">
          [{error.code}] {error.message}
        </div>
      )}

      <button
        onClick={handleSubmit}
        disabled={submitting}
        className="mt-8 flex w-full items-center justify-center gap-2 rounded-lg bg-black py-4 font-medium text-white disabled:opacity-50"
      >
        {submitting && (
          <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
        )}
        {submitting ? "주문 처리 중..." : "결제하기"}
      </button>
    </div>
  );
}
