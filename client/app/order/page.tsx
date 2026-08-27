"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useCart, CartItem } from "@/context/CartContext";
import { orderApi } from "@/api/orderApi";
import { OrderCreateResponse } from "@/types/order";
import OrderStepper from "@/components/OrderStepper";

type Phase = "review" | "paying" | "done";

export default function OrderPage() {
  const router = useRouter();
  const { items, totalPrice, clearCart } = useCart();

  const [phase, setPhase] = useState<Phase>("review");

  const [email, setEmail] = useState("");
  const [addressLine1, setAddressLine1] = useState("");
  const [addressLine2, setAddressLine2] = useState("");
  const [postalCode, setPostalCode] = useState("");

  const [error, setError] = useState<{ code: number; message: string } | null>(
    null
  );

  // 주문 완료 화면에서 보여줄 스냅샷 (clearCart 하면 장바구니가 비니까 미리 따로 저장해둠)
  const [orderResult, setOrderResult] = useState<OrderCreateResponse | null>(
    null
  );
  const [confirmedItems, setConfirmedItems] = useState<CartItem[]>([]);
  const [confirmedTotal, setConfirmedTotal] = useState(0);

  const handleSubmit = async () => {
    if (phase === "paying") return;
    setError(null);
    setPhase("paying");

    // 실제 결제 연동이 없어서, 결제 처리하는 척 1.5초 정도 대기시킴 (연출용)
    await new Promise((resolve) => setTimeout(resolve, 1500));

    try {
      const result = await orderApi.create({
        email,
        addressLine1,
        addressLine2,
        postalCode,
        items: items.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
        })),
      });

      setOrderResult(result);
      setConfirmedItems(items);
      setConfirmedTotal(totalPrice);
      clearCart();
      setPhase("done");
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
      setPhase("review"); // 실패하면 다시 입력 화면으로
    }
  };

  // 결제 처리 중 (연출용 스피너)
  if (phase === "paying") {
    return (
      <div className="mx-auto max-w-2xl px-8 py-10">
        <OrderStepper currentStep={3} />
        <div className="flex flex-col items-center justify-center gap-4 py-24">
          <div className="h-10 w-10 animate-spin rounded-full border-4 border-gray-200 border-t-gray-800" />
          <p className="text-gray-500">결제 처리 중입니다...</p>
        </div>
      </div>
    );
  }

  // 주문 완료 화면
  if (phase === "done" && orderResult) {
    return (
      <div className="mx-auto max-w-2xl px-8 py-10">
        <OrderStepper currentStep={4} />

        <div className="flex flex-col items-center gap-2 py-6 text-center">
          <p className="text-xl font-bold text-black">결제가 완료되었습니다</p>
          <p className="text-sm text-gray-500">
            주문번호 {orderResult.orderId} · {orderResult.status}
          </p>
        </div>

        <div className="mb-8 flex flex-col gap-3 rounded-lg bg-gray-50 p-4">
          {confirmedItems.map((item) => (
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
            <span>{confirmedTotal.toLocaleString()}원</span>
          </div>
        </div>

        {/* request로 보냈던 배송 정보 전체 표시 */}
        <div className="mb-8 flex flex-col gap-2 rounded-lg bg-gray-50 p-4 text-sm text-black">
          <div className="flex justify-between">
            <span className="text-gray-500">이메일</span>
            <span>{email}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500">주소</span>
            <span>{addressLine1}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500">상세주소</span>
            <span>{addressLine2}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500">우편번호</span>
            <span>{postalCode}</span>
          </div>
        </div>

        <button
          onClick={() => router.push("/menu")}
          className="w-full rounded-lg bg-black py-4 font-medium text-white"
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

  // 주문확인 (기본 화면)
  return (
    <div className="mx-auto max-w-2xl px-8 py-10 pb-32">
      <OrderStepper currentStep={2} />
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
        className="mt-8 w-full rounded-lg bg-black py-4 font-medium text-white"
      >
        결제하기
      </button>
    </div>
  );
}
