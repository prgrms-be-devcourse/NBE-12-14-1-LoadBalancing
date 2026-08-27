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
  const [deletingId, setDeletingId] = useState<number | null>(null);

  // 검색창에 타이핑 중인 값(keyword)과, 실제로 마지막에 "검색"을 눌러서 조회에 쓰인 값(appliedKeyword)을 분리
  // -> 타이핑할 때마다 요청 안 나가고, 검색 버튼/엔터 눌렀을 때만 조회
  const [keyword, setKeyword] = useState("");
  const [appliedKeyword, setAppliedKeyword] = useState("");

  const [editingStockId, setEditingStockId] = useState<number | null>(null);
  const [stockInput, setStockInput] = useState("");
  const [savingStock, setSavingStock] = useState(false);

  const fetchPage = useCallback(
    async (targetPage: number, searchKeyword: string) => {
      setLoading(true);
      setError(null);

      try {
        const res = await productApi.getList(
          targetPage,
          PAGE_SIZE,
          searchKeyword
        );
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
    },
    []
  );

  useEffect(() => {
    fetchPage(0, appliedKeyword);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [appliedKeyword]);

  const handleSearch = () => {
    setAppliedKeyword(keyword.trim());
  };

  const handleDelete = async (productId: number, title: string) => {
    if (!window.confirm(`"${title}" 상품을 삭제하시겠습니까?`)) return;

    setDeletingId(productId);
    setError(null);

    try {
      await productApi.delete(productId);
      await fetchPage(page, appliedKeyword); // 삭제 후 같은 페이지 다시 불러오기
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
      setDeletingId(null);
    }
  };

  const startEditStock = (productId: number, currentStock: number) => {
    setEditingStockId(productId);
    setStockInput(String(currentStock));
  };

  const cancelEditStock = () => {
    setEditingStockId(null);
    setStockInput("");
  };

  const saveStock = async (productId: number) => {
    if (savingStock) return;
    setSavingStock(true);
    setError(null);

    try {
      const newStock = Number(stockInput);
      await productApi.updateStock(productId, newStock);
      // API는 204라 응답에 값이 없어서, 화면에서만 직접 값 갱신
      setProducts((prev) =>
        prev.map((p) => (p.id === productId ? { ...p, stock: newStock } : p))
      );
      setEditingStockId(null);
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
      setSavingStock(false);
    }
  };

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

      <div className="mb-6 flex gap-2">
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") handleSearch();
          }}
          placeholder="상품명으로 검색"
          className="flex-1 rounded border border-gray-300 px-3 py-2 text-sm text-black"
        />
        <button
          onClick={handleSearch}
          className="rounded bg-black px-4 py-2 text-sm font-medium text-white"
        >
          검색
        </button>
        {appliedKeyword && (
          <button
            onClick={() => {
              setKeyword("");
              setAppliedKeyword("");
            }}
            className="rounded border border-gray-200 px-4 py-2 text-sm font-medium text-black hover:bg-gray-50"
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
            onClick={() => fetchPage(page, appliedKeyword)}
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
                <th className="py-3 font-medium"></th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr
                  key={product.id}
                  onClick={() =>
                    router.push(`/admin/products/${product.id}/edit`)
                  }
                  className="cursor-pointer border-b border-gray-100 hover:bg-gray-50"
                >
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
                    {editingStockId === product.id ? (
                      <div
                        className="flex items-center gap-1"
                        onClick={(e) => e.stopPropagation()} // 이 안에서 클릭해도 행 이동 안 되게
                      >
                        {/* type=number라 브라우저 기본 위/아래 화살표로 증감 가능 + 직접 숫자 입력도 가능 */}
                        <input
                          type="number"
                          value={stockInput}
                          onChange={(e) => setStockInput(e.target.value)}
                          className="w-16 rounded border border-gray-300 px-1 py-0.5 text-black"
                          autoFocus
                        />
                        <button
                          onClick={() => saveStock(product.id)}
                          disabled={savingStock}
                          className="rounded bg-black px-2 py-0.5 text-xs text-white disabled:opacity-50"
                        >
                          저장
                        </button>
                        <button
                          onClick={cancelEditStock}
                          className="rounded bg-gray-100 px-2 py-0.5 text-xs text-black"
                        >
                          취소
                        </button>
                      </div>
                    ) : product.stock === 0 ? (
                      <span className="text-red-500">품절</span>
                    ) : (
                      product.stock
                    )}
                  </td>
                  <td className="py-3 text-right">
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        startEditStock(product.id, product.stock);
                      }}
                      className="mr-2 rounded bg-gray-100 px-2 py-1 text-xs font-medium text-black hover:bg-gray-200"
                    >
                      재고수정
                    </button>
                    <button
                      onClick={(e) => {
                        e.stopPropagation(); // 행 클릭(수정페이지 이동)이랑 겹치지 않게
                        handleDelete(product.id, product.title);
                      }}
                      disabled={deletingId === product.id}
                      className="rounded bg-red-50 px-2 py-1 text-xs font-medium text-red-600 hover:bg-red-100 disabled:opacity-50"
                    >
                      {deletingId === product.id ? "삭제 중..." : "삭제"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* 페이지 이동 (무한스크롤 아니고 이전/다음 버튼 방식) */}
          <div className="mt-6 flex items-center justify-center gap-4">
            <button
              onClick={() => fetchPage(page - 1, appliedKeyword)}
              disabled={page === 0}
              className="rounded border border-gray-200 px-3 py-1 text-black disabled:opacity-30"
            >
              이전
            </button>
            <span className="text-sm text-gray-500">
              {page + 1} / {totalPages}
            </span>
            <button
              onClick={() => fetchPage(page + 1, appliedKeyword)}
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
