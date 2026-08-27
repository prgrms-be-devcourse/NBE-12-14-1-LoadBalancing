"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { adminApi } from "@/api/adminApi";
import { AdminDashboardResponse } from "@/types/admin";
import { ProductInfo } from "@/types/product";
import AdminNav from "@/components/AdminNav";
import Icon from "@/components/Icon";

export default function AdminDashboardPage() {
  const router = useRouter();
  const [data, setData] = useState<AdminDashboardResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<{ code: number; message: string } | null>(
    null
  );

  const fetchDashboard = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const res = await adminApi.getDashboard();
      setData(res);
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
  }, []);

  useEffect(() => {
    fetchDashboard();
  }, [fetchDashboard]);

  return (
    <div className="mx-auto w-[896px] px-8 py-10">
      <AdminNav />

      <div className="mb-8 flex items-center justify-between">
        <h1 className="text-headline-md font-bold text-black">대시보드</h1>
        {/* 기준: 오후 2시 컷오프 (주문 생성 로직이랑 동일) */}
        <span className="text-label-sm text-gray-400">
          매출/주문 통계는 매일 오후 2시 기준으로 집계돼요
        </span>
      </div>

      {error && (
        <div className="mb-6 flex items-center justify-between rounded-lg bg-red-50 p-4 text-sm text-red-600">
          <span>
            [{error.code}] {error.message}
          </span>
          <button
            onClick={fetchDashboard}
            className="ml-4 rounded bg-red-100 px-3 py-1 font-medium hover:bg-red-200"
          >
            다시 시도
          </button>
        </div>
      )}

      {loading && (
        <div className="flex justify-center py-16">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-gray-200 border-t-black" />
        </div>
      )}

      {!loading && !error && data && (
        <div className="flex flex-col gap-10">
          {/* 매출/주문 현황 - 오늘을 강조(검은 카드), 이번 주/이번 달은 보조 */}
          <div>
            <h2 className="mb-3 flex items-center gap-2 text-label-lg font-bold uppercase tracking-wide text-black">
              <Icon name="payments" />
              매출 현황
            </h2>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              <SalesCard
                label="오늘"
                totalSales={data.dailyTotalSales}
                orderCount={data.dailyOrderCount}
                averageAmount={data.dailyAverageOrderAmount}
                emphasis
              />
              <SalesCard
                label="이번 주"
                totalSales={data.weeklyTotalSales}
                orderCount={data.weeklyOrderCount}
                averageAmount={data.weeklyAverageOrderAmount}
              />
              <SalesCard
                label="이번 달"
                totalSales={data.monthlyTotalSales}
                orderCount={data.monthlyOrderCount}
                averageAmount={data.monthlyAverageOrderAmount}
              />
            </div>
          </div>

          {/* 주문 상태별 현황 */}
          <div>
            <h2 className="mb-3 flex items-center gap-2 text-label-lg font-bold uppercase tracking-wide text-black">
              <Icon name="list_alt" />
              주문 상태별 현황
            </h2>
            {data.orderStatusCounts.length === 0 ? (
              <p className="text-sm text-gray-400">주문이 없습니다.</p>
            ) : (
              <div className="flex flex-wrap gap-3">
                {data.orderStatusCounts.map((s) => (
                  <div
                    key={s.status}
                    className="flex items-center gap-2 rounded-lg border border-gray-200 px-4 py-2"
                  >
                    <span className="text-label-sm text-gray-500">
                      {s.description}
                    </span>
                    <span className="text-body-md font-bold text-black">
                      {s.count.toLocaleString()}건
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* 전체 상품 수 - 요약 카드 하나짜리라 크게 */}
          <div className="flex items-center gap-4 rounded-lg border border-gray-200 p-6">
            <Icon name="inventory_2" className="text-4xl text-gray-400" />
            <div>
              <p className="text-label-sm text-gray-500">전체 상품 수</p>
              <p className="text-headline-md font-extrabold text-black">
                {data.totalProductCount.toLocaleString()}개
              </p>
            </div>
          </div>

          <ProductSection
            title="품절 상품"
            icon="production_quantity_limits"
            emptyText="품절된 상품이 없습니다."
            products={data.outOfStockProducts}
            onClickProduct={(id) => router.push(`/admin/products/${id}/edit`)}
            badge={() => (
              <span className="text-label-sm font-bold uppercase text-red-500">
                품절
              </span>
            )}
          />

          <ProductSection
            title="재고 부족 상품 (1~10개)"
            icon="warning"
            emptyText="재고가 부족한 상품이 없습니다."
            products={data.lowStockProducts}
            onClickProduct={(id) => router.push(`/admin/products/${id}/edit`)}
            badge={(p) => (
              <span className="text-label-sm font-bold text-orange-500">
                재고 {p.stock}개
              </span>
            )}
          />

          <ProductSection
            title="최근 등록 상품"
            icon="new_releases"
            emptyText="등록된 상품이 없습니다."
            products={data.recentProducts}
            onClickProduct={(id) => router.push(`/admin/products/${id}/edit`)}
            badge={(p) => (
              <span className="text-label-sm text-gray-400">
                {new Date(p.createdAt).toLocaleDateString()}
              </span>
            )}
          />
        </div>
      )}
    </div>
  );
}

function SalesCard({
  label,
  totalSales,
  orderCount,
  averageAmount,
  emphasis = false,
}: {
  label: string;
  totalSales: number;
  orderCount: number;
  averageAmount: number;
  emphasis?: boolean;
}) {
  return (
    <div
      className={`rounded-lg p-5 ${
        emphasis
          ? "bg-black text-white"
          : "border border-gray-200 text-black"
      }`}
    >
      <p
        className={`text-label-sm ${emphasis ? "text-white/70" : "text-gray-500"}`}
      >
        {label}
      </p>
      <p className="text-headline-md mt-1 font-extrabold">
        {totalSales.toLocaleString()}원
      </p>
      <div
        className={`text-label-sm mt-3 flex justify-between ${
          emphasis ? "text-white/70" : "text-gray-500"
        }`}
      >
        <span>주문 {orderCount.toLocaleString()}건</span>
        <span>평균 {averageAmount.toLocaleString()}원</span>
      </div>
    </div>
  );
}

function ProductSection({
  title,
  icon,
  emptyText,
  products,
  onClickProduct,
  badge,
}: {
  title: string;
  icon: string;
  emptyText: string;
  products: ProductInfo[];
  onClickProduct: (id: number) => void;
  badge: (product: ProductInfo) => React.ReactNode;
}) {
  return (
    <div>
      <h2 className="mb-3 flex items-center gap-2 text-label-lg font-bold uppercase tracking-wide text-black">
        <Icon name={icon} />
        {title}{" "}
        <span className="text-body-md font-normal text-gray-400">
          ({products.length})
        </span>
      </h2>

      {products.length === 0 ? (
        <p className="text-sm text-gray-400">{emptyText}</p>
      ) : (
        <div className="flex flex-col gap-2">
          {products.map((product) => (
            <div
              key={product.id}
              onClick={() => onClickProduct(product.id)}
              className="flex cursor-pointer items-center justify-between rounded-lg border border-gray-100 px-4 py-3 hover:bg-gray-50"
            >
              <div className="flex items-center gap-3">
                <div className="h-10 w-10 shrink-0 overflow-hidden rounded bg-gray-100">
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
                        className="text-base text-gray-300"
                      />
                    </div>
                  )}
                </div>
                <div>
                  <p className="text-body-md font-bold text-black">
                    {product.title}
                  </p>
                  <p className="text-label-sm text-gray-500">
                    {product.price.toLocaleString()}원
                  </p>
                </div>
              </div>
              {badge(product)}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
