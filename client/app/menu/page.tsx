"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { productApi } from "@/api/productApi";
import { ProductInfo } from "@/types/product";
import OrderStepper from "@/components/OrderStepper";

export default function MenuPage() {
  const router = useRouter();
  const [products, setProducts] = useState<ProductInfo[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<{ code: number; message: string } | null>(
    null
  );

  const sentinelRef = useRef<HTMLDivElement | null>(null);
  // loading은 state라서 반영되기까지 시차가 있어 동시 호출을 못 막음.
  // ref는 그 자리에서 바로 바뀌므로 중복 호출 방지용으로 따로 둠.
  const loadingRef = useRef(false);

  const loadMore = useCallback(async () => {
    if (loadingRef.current || !hasMore) return;
    loadingRef.current = true;
    setLoading(true);

    try {
      const res = await productApi.getList(page);
      setProducts((prev) => [...prev, ...res.content]);
      setHasMore(!res.last);
      setPage((prev) => prev + 1);
      setError(null);
    } catch (e) {
      // 백엔드가 실패 시 { success:false, code, message } 형태로 내려주는 걸 그대로 보여줌
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
      loadingRef.current = false;
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, hasMore]);

  // 첫 진입 시 1페이지 로드
  useEffect(() => {
    loadMore();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 스크롤이 맨 아래 sentinel까지 내려오면 다음 페이지 자동 로드
  useEffect(() => {
    if (!sentinelRef.current) return;

    const observer = new IntersectionObserver((entries) => {
      // 에러난 상태에서는 자동 재시도 안 함 (무한 재시도 방지), 버튼으로만 재시도
      if (entries[0].isIntersecting && !error) {
        loadMore();
      }
    });

    observer.observe(sentinelRef.current);
    return () => observer.disconnect();
  }, [loadMore, error]);

  return (
    <div className="min-h-screen bg-white px-8 py-10">
      <OrderStepper currentStep={1} />
      <h1 className="mb-8 text-2xl font-bold text-black">메뉴</h1>

      {error && (
        <div className="mb-6 flex items-center justify-between rounded-lg bg-red-50 p-4 text-sm text-red-600">
          <span>
            [{error.code}] {error.message}
          </span>
          <button
            onClick={() => loadMore()}
            className="ml-4 rounded bg-red-100 px-3 py-1 font-medium hover:bg-red-200"
          >
            다시 시도
          </button>
        </div>
      )}

      {products.length === 0 && !loading && !error && (
        <p className="text-gray-500">상품이 없습니다.</p>
      )}

      <div className="grid grid-cols-2 gap-x-6 gap-y-10 sm:grid-cols-3 md:grid-cols-4">
        {products.map((product) => (
          <div
            key={product.id}
            onClick={() => router.push(`/menu/${product.id}`)}
            className="flex cursor-pointer flex-col gap-2"
          >
            <div className="aspect-square overflow-hidden rounded-xl bg-gray-100">
              {product.thumbnail ? (
                <img
                  src={product.thumbnail}
                  alt={product.title}
                  className="h-full w-full object-cover"
                />
              ) : (
                <div className="flex h-full w-full items-center justify-center text-5xl">
                  🚫
                </div>
              )}
            </div>
            <p className="font-medium text-black">{product.title}</p>
            <p className="text-sm text-gray-500">
              {product.price.toLocaleString()}원
            </p>
          </div>
        ))}
      </div>

      {loading && (
        <div className="flex justify-center py-8">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-gray-200 border-t-gray-800" />
        </div>
      )}

      {/* 이 div가 화면에 보이면(스크롤이 여기까지 오면) 다음 페이지 자동 로드됨 */}
      <div ref={sentinelRef} className="h-1" />
    </div>
  );
}
