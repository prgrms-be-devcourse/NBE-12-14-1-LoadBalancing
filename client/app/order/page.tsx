"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useCart } from "@/context/CartContext";
import { orderApi } from "@/api/orderApi";
import { OrderInfo, getOrderTotal } from "@/types/order";
import OrderStepper from "@/components/OrderStepper";
import BackToHomeButton from "@/components/BackToHomeButton";
import Icon from "@/components/Icon";

type Phase = "review" | "paying" | "done";

export default function OrderPage() {
  const router = useRouter();
  const { items, totalPrice, updateQuantity, removeItem, clearCart } =
    useCart();

  const [phase, setPhase] = useState<Phase>("review");

  const [email, setEmail] = useState("");
  const [addressLine1, setAddressLine1] = useState("");
  const [addressLine2, setAddressLine2] = useState("");
  const [postalCode, setPostalCode] = useState("");

  // 서버 호출 실패 등 "필드 하나로 딱 떨어지지 않는" 에러용 (검증 실패는 아래 fieldErrors로 따로 뺌)
  const [error, setError] = useState<{ code: number; message: string } | null>(
    null
  );

  type FieldErrors = {
    email?: string;
    addressLine1?: string;
    addressLine2?: string;
    postalCode?: string;
  };
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});

  // 백엔드 검증 규칙(OrderRequest.OrderCreate)이랑 최대한 맞춰서, 서버까지 안 갔다 오고
  // 바로 알려줌. 그래도 최종 검증은 서버가 다시 하니까(프론트만 믿을 수 없어서) 이건 UX용 보조 검증.
  // 필드별로 따로 반환해서, 각 입력창 바로 아래에 그 칸만의 에러를 보여줄 수 있게 함
  const validate = (): FieldErrors => {
    const errors: FieldErrors = {};

    if (!email.trim()) {
      errors.email = "이메일을 입력해주세요.";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      errors.email = "올바른 이메일 형식이 아닙니다.";
    }

    if (!addressLine1.trim()) {
      errors.addressLine1 = "주소를 입력해주세요.";
    }

    if (!addressLine2.trim()) {
      errors.addressLine2 = "상세주소를 입력해주세요.";
    }

    if (!postalCode.trim()) {
      errors.postalCode = "우편번호를 입력해주세요.";
    } else if (!/^\d{5}$/.test(postalCode.trim())) {
      errors.postalCode = "우편번호는 숫자 5자리여야 합니다.";
    }

    return errors;
  };

  // 서버가 실제로 저장한(같은 이메일로 컷오프 안에 병합됐으면 그것까지 합쳐진) 전체 주문 정보.
  // 예전엔 프론트가 보낸 값을 그대로 다시 보여줬었는데, 이제 서버 응답을 그대로 씀
  const [orderResult, setOrderResult] = useState<OrderInfo | null>(null);

  // 주문 완료 화면에서 상품 사진을 보여주기 위한 것 - 서버 응답(OrderItemInfo)엔 썸네일이 안 내려와서,
  // clearCart로 지워지기 전에 장바구니에 있던 productId -> thumbnail을 미리 저장해둠
  const [thumbnails, setThumbnails] = useState<Record<number, string>>({});

  const handleSubmit = async () => {
    if (phase === "paying") return;
    setError(null);

    const errors = validate();
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) return;

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
      setThumbnails(
        Object.fromEntries(items.map((item) => [item.productId, item.thumbnail]))
      );
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
      <div className="mx-auto w-full max-w-7xl">
        <header className="mb-8 border-b-2 border-black">
          <Link href="/" className="flex h-20 items-center justify-center">
            <span className="text-headline-md font-extrabold uppercase tracking-tighter text-black">
              Kiosk
            </span>
          </Link>
        </header>

        <div className="px-8 pb-10">
          <OrderStepper currentStep={3} />
          <div className="flex flex-col items-center justify-center gap-4 py-24">
            <div className="h-10 w-10 animate-spin rounded-full border-4 border-gray-200 border-t-black" />
            <p className="text-body-md text-gray-500">결제 처리 중입니다...</p>
          </div>
        </div>
      </div>
    );
  }

  // 주문 완료 화면
  if (phase === "done" && orderResult) {
    return (
      <div className="mx-auto w-full max-w-7xl">
        <header className="mb-8 border-b-2 border-black">
          <Link href="/" className="flex h-20 items-center justify-center">
            <span className="text-headline-md font-extrabold uppercase tracking-tighter text-black">
              Kiosk
            </span>
          </Link>
        </header>

        <div className="flex w-full flex-col items-center px-8 pb-10 text-center">
        <OrderStepper currentStep={4} />

        <div className="flex w-full flex-col items-center gap-4 py-6">
          <div className="flex h-16 w-16 items-center justify-center rounded-full bg-black">
            <Icon name="check" className="text-4xl text-white" filled />
          </div>
          <div>
            <p className="text-headline-md font-bold text-black">
              주문이 완료되었습니다
            </p>
            <p className="text-body-md mt-1 text-gray-500">
              결제가 정상적으로 처리됐어요
            </p>
          </div>

          <div className="w-full max-w-xs rounded-lg border border-gray-200 py-6">
            <p className="text-label-sm font-bold uppercase tracking-wide text-gray-400">
              주문번호
            </p>
            <p className="text-headline-lg mt-1 font-extrabold text-black">
              #{orderResult.orderId}
            </p>
          </div>
        </div>

        <div className="mb-6 flex w-full flex-col gap-3 rounded-lg bg-gray-50 p-4 text-left">
          {/* orderResult.items는 서버가 실제로 저장한 전체 아이템 - 같은 이메일로 오늘 이미
              주문한 게 있었으면 이번에 담은 것뿐 아니라 그것까지 다 합쳐져서 나옴 */}
          {orderResult.items.map((item, index) => (
            <div
              // 같은 이메일로 오늘 두 번 주문하면 같은 productId가 별도 줄로 합쳐져 들어올 수 있어서
              // (orders/admin-orders 페이지랑 동일한 이유) index를 같이 섞어 key를 고유하게 만듦
              key={`${item.productId}-${index}`}
              className="flex items-center gap-3"
            >
              <div className="h-12 w-12 shrink-0 overflow-hidden rounded-md bg-gray-100">
                {thumbnails[item.productId] ? (
                  <img
                    src={thumbnails[item.productId]}
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

              <span className="text-body-md flex-1 truncate font-medium text-black">
                {item.title} x {item.quantity}
              </span>
              <span className="text-body-md text-black">
                {(item.price * item.quantity).toLocaleString()}원
              </span>
            </div>
          ))}
          <div className="text-body-md mt-2 flex justify-between border-t border-gray-200 pt-2 font-bold text-black">
            <span>총 금액</span>
            <span>{getOrderTotal(orderResult).toLocaleString()}원</span>
          </div>
        </div>

        {/* 서버에 실제로 저장된 배송 정보 (프론트 입력값 재사용 아님) */}
        <div className="mb-8 flex w-full flex-col gap-2 rounded-lg bg-gray-50 p-4 text-left text-sm text-black">
          <div className="flex justify-between">
            <span className="text-gray-500">이메일</span>
            <span>{orderResult.email}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500">주소</span>
            <span>{orderResult.addressLine1}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500">상세주소</span>
            <span>{orderResult.addressLine2}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500">우편번호</span>
            <span>{orderResult.postalCode}</span>
          </div>
        </div>

        <button
          onClick={() => router.push("/menu")}
          className="touch-target w-full rounded-lg bg-black font-bold text-white"
        >
          메뉴로 돌아가기
        </button>
        </div>
      </div>
    );
  }

  // 장바구니가 비어있는데 이 페이지로 바로 들어온 경우
  if (items.length === 0) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-4">
        <BackToHomeButton />
        <p className="text-gray-500">장바구니가 비어있습니다.</p>
        <button
          onClick={() => router.push("/menu")}
          className="touch-target rounded-lg bg-black px-6 font-bold text-white"
        >
          메뉴 보러가기
        </button>
      </div>
    );
  }

  // 주문확인 (기본 화면) - zz/_1 카트 화면 참고, 왼쪽엔 상품 목록 / 오른쪽엔 입력폼+총액+결제 2단 구성
  return (
    <div className="mx-auto w-full max-w-7xl">
      <header className="mb-8 border-b-2 border-black">
        <Link href="/" className="flex h-20 items-center justify-center">
          <span className="text-headline-md font-extrabold uppercase tracking-tighter text-black">
            Kiosk
          </span>
        </Link>
      </header>

      <div className="px-8 pb-12">
      <OrderStepper currentStep={2} />
      <h1 className="text-headline-md mb-8 font-bold text-black">주문하기</h1>

      <div className="grid grid-cols-1 gap-10 lg:grid-cols-[1.3fr_1fr]">
        {/* 왼쪽: 담은 상품 목록 (수량 변경/삭제 바로 가능) */}
        <div>
          <h2 className="text-label-lg mb-3 font-bold text-black">
            주문 상품 ({items.length})
          </h2>
          <div className="flex flex-col divide-y divide-gray-100 border-y border-gray-200">
            {items.map((item) => (
              <div key={item.productId} className="flex items-center gap-4 py-4">
                <div className="h-20 w-20 shrink-0 overflow-hidden rounded-lg bg-gray-100">
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
                        className="text-2xl text-gray-300"
                      />
                    </div>
                  )}
                </div>

                <div className="flex-1">
                  <p className="text-body-md font-bold text-black">
                    {item.title}
                  </p>
                  <p className="text-label-sm mt-1 text-gray-500">
                    {item.price.toLocaleString()}원
                  </p>
                </div>

                <div className="flex items-center gap-3 rounded-lg border border-gray-200">
                  <button
                    onClick={() =>
                      updateQuantity(item.productId, item.quantity - 1)
                    }
                    className="touch-target flex w-10 items-center justify-center text-black"
                  >
                    <Icon name="remove" className="text-lg" />
                  </button>
                  <span className="text-body-md w-4 text-center font-bold text-black">
                    {item.quantity}
                  </span>
                  <button
                    onClick={() =>
                      updateQuantity(item.productId, item.quantity + 1)
                    }
                    disabled={item.quantity >= item.stock}
                    className="touch-target flex w-10 items-center justify-center text-black disabled:opacity-30"
                  >
                    <Icon name="add" className="text-lg" />
                  </button>
                </div>

                <span className="text-body-md w-20 text-right font-bold text-black">
                  {(item.price * item.quantity).toLocaleString()}원
                </span>

                <button
                  onClick={() => removeItem(item.productId)}
                  className="touch-target flex items-center justify-center text-gray-400 hover:text-red-500"
                >
                  <Icon name="delete" className="text-lg" />
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* 오른쪽: 위부터 배송 정보 입력 -> 총액 -> 결제 버튼 순서 */}
        <div className="flex flex-col gap-6">
          <div>
            <h2 className="text-label-lg mb-3 font-bold text-black">
              배송 정보
            </h2>
            <div className="flex flex-col gap-2">
              <div>
                <input
                  type="email"
                  placeholder="이메일"
                  value={email}
                  onChange={(e) => {
                    setEmail(e.target.value);
                    if (fieldErrors.email)
                      setFieldErrors((prev) => ({
                        ...prev,
                        email: undefined,
                      }));
                  }}
                  className={`text-body-md w-full rounded-lg border px-4 py-3 text-black ${
                    fieldErrors.email ? "border-red-500" : "border-gray-200"
                  }`}
                />
                {/* 에러 메시지 자리를 항상 확보해둬서, 떴다 안 떴다 할 때 아래 요소들이 밀리지 않게 함 */}
                <p className="mt-1 min-h-[18px] text-xs text-red-600">
                  {fieldErrors.email}
                </p>
              </div>

              <div>
                <input
                  type="text"
                  placeholder="주소"
                  value={addressLine1}
                  onChange={(e) => {
                    setAddressLine1(e.target.value);
                    if (fieldErrors.addressLine1)
                      setFieldErrors((prev) => ({
                        ...prev,
                        addressLine1: undefined,
                      }));
                  }}
                  className={`text-body-md w-full rounded-lg border px-4 py-3 text-black ${
                    fieldErrors.addressLine1
                      ? "border-red-500"
                      : "border-gray-200"
                  }`}
                />
                <p className="mt-1 min-h-[18px] text-xs text-red-600">
                  {fieldErrors.addressLine1}
                </p>
              </div>

              <div>
                <input
                  type="text"
                  placeholder="상세주소"
                  value={addressLine2}
                  onChange={(e) => {
                    setAddressLine2(e.target.value);
                    if (fieldErrors.addressLine2)
                      setFieldErrors((prev) => ({
                        ...prev,
                        addressLine2: undefined,
                      }));
                  }}
                  className={`text-body-md w-full rounded-lg border px-4 py-3 text-black ${
                    fieldErrors.addressLine2
                      ? "border-red-500"
                      : "border-gray-200"
                  }`}
                />
                <p className="mt-1 min-h-[18px] text-xs text-red-600">
                  {fieldErrors.addressLine2}
                </p>
              </div>

              <div>
                <input
                  type="text"
                  inputMode="numeric"
                  maxLength={5}
                  placeholder="우편번호 (숫자 5자리)"
                  value={postalCode}
                  onChange={(e) => {
                    setPostalCode(e.target.value.replace(/[^0-9]/g, ""));
                    if (fieldErrors.postalCode)
                      setFieldErrors((prev) => ({
                        ...prev,
                        postalCode: undefined,
                      }));
                  }}
                  className={`text-body-md w-full rounded-lg border px-4 py-3 text-black ${
                    fieldErrors.postalCode
                      ? "border-red-500"
                      : "border-gray-200"
                  }`}
                />
                <p className="mt-1 min-h-[18px] text-xs text-red-600">
                  {fieldErrors.postalCode}
                </p>
              </div>
            </div>
          </div>

          <div className="rounded-lg border-2 border-black p-5">
            <div className="text-body-md flex justify-between font-bold text-black">
              <span>총 금액</span>
              <span>{totalPrice.toLocaleString()}원</span>
            </div>
          </div>

          {error && (
            <div className="rounded-lg bg-red-50 p-4 text-sm text-red-600">
              [{error.code}] {error.message}
            </div>
          )}

          <button
            onClick={handleSubmit}
            className="touch-target w-full rounded-lg bg-black font-bold text-white"
          >
            결제하기
          </button>
        </div>
      </div>
      </div>
    </div>
  );
}
