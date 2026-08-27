"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { productApi } from "@/api/productApi";
import { ProductInfo } from "@/types/product";
import { useCart } from "@/context/CartContext";
import OrderStepper from "@/components/OrderStepper";
import BackToHomeButton from "@/components/BackToHomeButton";

export default function ProductDetailPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const { addItem } = useCart();
  const [product, setProduct] = useState<ProductInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(0); // 지금 큰 이미지로 보여줄 인덱스
  const [quantity, setQuantity] = useState(1);
  const [added, setAdded] = useState(false);

  useEffect(() => {
    const id = Number(params.id);
    setLoading(true);
    setNotFound(false);
    setSelectedIndex(0);

    productApi
      .getDetail(id)
      .then((data) => setProduct(data))
      .catch(() => setNotFound(true))
      .finally(() => setLoading(false));
  }, [params.id]);

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-gray-200 border-t-gray-800" />
      </div>
    );
  }

  if (notFound || !product) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <p className="text-gray-500">해당 상품을 찾을 수 없습니다.</p>
      </div>
    );
  }

  // 썸네일을 맨 앞에 두고, 그 뒤에 productImg들을 이어붙임 (왼쪽 리스트용)
  const gallery = [
    { id: "thumbnail", url: product.thumbnail },
    ...product.imgs.map((img) => ({ id: String(img.id), url: img.url })),
  ];

  const fadeClass = added ? "opacity-0" : "opacity-100";

  return (
    <div className="mx-auto max-w-5xl px-8 py-10">
      <BackToHomeButton />
      <OrderStepper currentStep={1} />

      <div className={`flex gap-12 transition-opacity duration-500 ${fadeClass}`}>
        {/* 왼쪽: 작은 썸네일 리스트 + 큰 이미지 */}
        <div className="flex gap-4">
          <div className="flex max-h-[480px] flex-col gap-2 overflow-y-auto">
            {gallery.map((img, index) => (
              <button
                key={img.id}
                onMouseEnter={() => setSelectedIndex(index)}
                className={`h-14 w-14 shrink-0 overflow-hidden rounded-lg bg-gray-100 ${
                  index === selectedIndex
                    ? "ring-2 ring-blue-500"
                    : "ring-1 ring-gray-200"
                }`}
              >
                {img.url ? (
                  <img
                    src={img.url}
                    alt=""
                    className="h-full w-full object-cover"
                  />
                ) : (
                  <div className="flex h-full w-full items-center justify-center text-xl">
                    🚫
                  </div>
                )}
              </button>
            ))}
          </div>

          <div className="aspect-square w-96 overflow-hidden rounded-xl bg-gray-100">
            {gallery[selectedIndex]?.url ? (
              <img
                src={gallery[selectedIndex].url}
                alt={product.title}
                className="h-full w-full object-cover"
              />
            ) : (
              <div className="flex h-full w-full items-center justify-center text-8xl">
                🚫
              </div>
            )}
          </div>
        </div>

        {/* 오른쪽: 상품 정보 */}
        <div className="flex-1 pt-2">
          <h1 className="text-2xl font-bold text-black">
            {product.title || ""}
          </h1>

          <p className="mt-4 text-2xl font-semibold text-black">
            {product.price ? `${product.price.toLocaleString()}원` : ""}
          </p>

          {product.stock === 0 && (
            <span className="mt-3 inline-block rounded bg-gray-200 px-3 py-1 text-sm font-medium text-gray-600">
              품절
            </span>
          )}

          <p className="mt-6 whitespace-pre-line text-gray-600">
            {product.description || ""}
          </p>

          {product.stock !== 0 && (
            <div className="mt-8 flex items-center gap-4">
              <div className="flex items-center gap-3 rounded-lg border border-gray-200 px-3 py-2">
                <button
                  onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                  className="h-6 w-6 text-black"
                >
                  -
                </button>
                <span className="w-6 text-center text-black">{quantity}</span>
                <button
                  onClick={() => setQuantity((q) => q + 1)}
                  className="h-6 w-6 text-black"
                >
                  +
                </button>
              </div>

              <button
                onClick={() => {
                  addItem(
                    {
                      productId: product.id,
                      title: product.title,
                      price: product.price,
                      thumbnail: product.thumbnail,
                    },
                    quantity
                  );
                  setAdded(true);
                  // 담았다는 걸 잠깐 보여주면서 페이드아웃하고, 메뉴 목록으로 이동
                  setTimeout(() => router.push("/menu"), 500);
                }}
                className="rounded-lg bg-black px-6 py-3 font-medium text-white"
              >
                {added ? "담았습니다 ✓" : "장바구니 담기"}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
