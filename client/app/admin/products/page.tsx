"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { productApi } from "@/api/productApi";
import { ProductInfo } from "@/types/product";

const PAGE_SIZE = 10;

export default function AdminProductListPage() {
  const router = useRouter();
  const [products, setProducts] = useState<ProductInfo[]>([]);
  const [page, setPage] = useState(0); // 0부터 시작
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<{ code: number; message: string } | null>(
    null
  );

  const fetchPage = useCallback(async (targetPage: number) => {
    setLoading(true);
    setError(null);

    try {
      const res = await productApi.getList(targetPage, PAGE_SIZE);
      setProducts(res.content);
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
  }, []);

  useEffect(() => {
    fetchPage(0);
  }, [fetchPage]);

  return (
    <div className="mx-auto max-w-4xl px-8 py-10">
      <div className="mb-8 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-black">상품 관리</h1>
        <button
          onClick={() => router.push("/admin/products/new")}
          className="rounded-lg bg-black px-4 py-2 text-sm font-medium text-white"
        >
          상품 생성
        </button>
      </div>

      {error && (
        <div className="mb-6 flex items-center justify-between rounded-lg bg-red-50 p-4 text-sm text-red-600">
          <span>
            [{error.code}] {error.message}
          </span>
          <button
            onClick={() => fetchPage(page)}
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

      {!loading && !error && products.length === 0 && (
        <p className="text-gray-500">상품이 없습니다.</p>
      )}

      {!loading && products.length > 0 && (
        <>
          <table className="w-full border-collapse text-sm">
            <thead>
              <tr className="border-b border-gray-200 text-left text-gray-500">
                <th className="py-3 font-medium">썸네일</th>
                <th className="py-3 font-medium">상품명</th>
                <th className="py-3 font-medium">가격</th>
                <th className="py-3 font-medium">재고</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id} className="border-b border-gray-100">
                  <td className="py-3">
                    <div className="h-10 w-10 overflow-hidden rounded bg-gray-100">
                      {product.thumbnail && (
                        <img
                          src={product.thumbnail}
                          alt={product.title}
                          className="h-full w-full object-cover"
                        />
                      )}
                    </div>
                  </td>
                  <td className="py-3 text-black">{product.title}</td>
                  <td className="py-3 text-black">
                    {product.price.toLocaleString()}원
                  </td>
                  <td className="py-3 text-black">
                    {product.stock === 0 ? (
                      <span className="text-red-500">품절</span>
                    ) : (
                      product.stock
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* 페이지 이동 (무한스크롤 아니고 이전/다음 버튼 방식) */}
          <div className="mt-6 flex items-center justify-center gap-4">
            <button
              onClick={() => fetchPage(page - 1)}
              disabled={page === 0}
              className="rounded border border-gray-200 px-3 py-1 text-black disabled:opacity-30"
            >
              이전
            </button>
            <span className="text-sm text-gray-500">
              {page + 1} / {totalPages}
            </span>
            <button
              onClick={() => fetchPage(page + 1)}
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
