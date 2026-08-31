"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { productApi } from "@/api/productApi";
import { ProductInfo } from "@/types/product";
import OrderStepper from "@/components/OrderStepper";
import Icon from "@/components/Icon";

export default function MenuPage() {
  const router = useRouter();
  const [products, setProducts] = useState<ProductInfo[]>([]);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<{ code: number; message: string } | null>(
    null
  );

  const sentinelRef = useRef<HTMLDivElement | null>(null);
  // loading은 state라서 반영되기까지 시차가 있어 동시 호출을 못 막음.
  // ref는 그 자리에서 바로 바뀌므로 중복 호출 방지용으로 따로 둠.
  const loadingRef = useRef(false);

  // 검색창에 타이핑 중인 값(keyword)과, 실제로 검색에 적용된 값(appliedKeyword) 분리
  // -> 타이핑마다 요청 안 나가고, 검색 눌렀을 때만 처음부터 다시 조회
  const [keyword, setKeyword] = useState("");
  const [appliedKeyword, setAppliedKeyword] = useState("");

  // 가격 범위 검색 - 최소/최대 중 하나만 입력해도 적용됨 (백엔드가 빈 쪽을 0~무제한으로 처리)
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [appliedMinPrice, setAppliedMinPrice] = useState<number | undefined>(
    undefined
  );
  const [appliedMaxPrice, setAppliedMaxPrice] = useState<number | undefined>(
    undefined
  );

  // page는 "다음에 불러올 페이지 번호"를 담는 ref로 관리.
  // appliedKeyword가 바뀌면 새로 검색해야 하는데, loadMore가 이전 page state를 클로저로 들고 있으면
  // 검색 시작 시점에 page를 0으로 리셋해도 다음 tick에야 반영돼서 꼬일 수 있어 ref로 즉시 동기화
  const pageRef = useRef(0);

  const loadMore = useCallback(
    async (reset = false) => {
      if (loadingRef.current) return;
      if (!reset && !hasMore) return;
      loadingRef.current = true;
      setLoading(true);

      const targetPage = reset ? 0 : pageRef.current;

      try {
        const res = await productApi.getList(
          targetPage,
          10,
          appliedKeyword,
          appliedMinPrice,
          appliedMaxPrice
        );
        setProducts((prev) =>
          reset ? res.content : [...prev, ...res.content]
        );
        setHasMore(!res.last);
        pageRef.current = targetPage + 1;
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
    },
    [hasMore, appliedKeyword, appliedMinPrice, appliedMaxPrice]
  );

  // 첫 진입 + 검색 조건이 바뀔 때마다 처음부터 다시 로드
  useEffect(() => {
    loadMore(true);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [appliedKeyword, appliedMinPrice, appliedMaxPrice]);

  const handleSearch = () => {
    setAppliedKeyword(keyword.trim());
    setAppliedMinPrice(minPrice.trim() === "" ? undefined : Number(minPrice));
    setAppliedMaxPrice(maxPrice.trim() === "" ? undefined : Number(maxPrice));
  };

  const hasAppliedSearch =
    appliedKeyword || appliedMinPrice !== undefined || appliedMaxPrice !== undefined;

  const handleResetSearch = () => {
    setKeyword("");
    setMinPrice("");
    setMaxPrice("");
    setAppliedKeyword("");
    setAppliedMinPrice(undefined);
    setAppliedMaxPrice(undefined);
  };

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
    <div className="mx-auto w-full min-h-screen max-w-7xl bg-white">
      {/* 인덱스 페이지처럼 헤더는 페이지 맨 위에 붙이고(패딩 없이), 패딩은 본문에만 줌 */}
      <header className="mb-8 border-b-2 border-black">
        <Link href="/" className="flex h-20 items-center justify-center">
          <span className="text-headline-md font-extrabold uppercase tracking-tighter text-black">
            Kiosk
          </span>
        </Link>
      </header>

      <div className="px-8 pb-10">
      <OrderStepper currentStep={1} />
      <h1 className="text-headline-md mb-8 font-bold text-black">메뉴</h1>

      <div className="mb-8 flex flex-wrap items-center gap-2">
        <div className="flex flex-1 items-center gap-2 border-b border-gray-300 px-1 py-2 focus-within:border-black">
          <Icon name="search" className="text-xl text-gray-400" />
          <input
            type="text"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") handleSearch();
            }}
            placeholder="메뉴 이름으로 검색"
            className="text-body-md w-full text-black outline-none placeholder:text-gray-400"
          />
        </div>

        {/* 가격 범위 - 하나만 입력해도 적용됨 */}
        <div className="flex items-center gap-1 border-b border-gray-300 px-1 py-2 focus-within:border-black">
          <input
            type="number"
            value={minPrice}
            onChange={(e) => setMinPrice(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") handleSearch();
            }}
            placeholder="최소가격"
            className="text-body-md w-24 text-black outline-none placeholder:text-gray-400"
          />
          <span className="text-gray-400">~</span>
          <input
            type="number"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") handleSearch();
            }}
            placeholder="최대가격"
            className="text-body-md w-24 text-black outline-none placeholder:text-gray-400"
          />
        </div>

        <button
          onClick={handleSearch}
          className="rounded bg-black px-5 py-2 text-label-sm font-bold text-white"
        >
          검색
        </button>
        {hasAppliedSearch && (
          <button
            onClick={handleResetSearch}
            className="rounded border-2 border-black px-5 py-2 text-label-sm font-bold text-black hover:bg-gray-50"
          >
            초기화
          </button>
        )}
      </div>

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
        <p className="text-gray-500">
          {hasAppliedSearch
            ? "검색 결과가 없습니다."
            : "상품이 없습니다."}
        </p>
      )}

      {/* zz/_3 참고 - 카드 크게(최대 3열), 상품명 더 크고 굵게. 사진 위/텍스트 아래 구조는 그대로 유지 */}
      <div className="grid grid-cols-2 gap-6 sm:grid-cols-4">
        {products.map((product) => (
          <div
            key={product.id}
            onClick={() => router.push(`/menu/${product.id}`)}
            className="flex cursor-pointer flex-col overflow-hidden rounded border border-gray-200 transition-colors hover:border-black"
          >
            <div className="aspect-square overflow-hidden bg-gray-100">
              {product.thumbnail ? (
                <img
                  src={product.thumbnail}
                  alt={product.title}
                  className="h-full w-full object-cover"
                />
              ) : (
                <div className="flex h-full w-full items-center justify-center">
                  <Icon
                    name="image_not_supported"
                    className="text-4xl text-gray-300"
                  />
                </div>
              )}
            </div>
            <div className="flex flex-col gap-1 border-t border-gray-100 px-4 py-4">
              <p className="text-body-lg font-bold text-black">
                {product.title}
              </p>
              <p className="text-body-md text-gray-500">
                {product.price.toLocaleString()}원
              </p>
            </div>
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
    </div>
  );
}
