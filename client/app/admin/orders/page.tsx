"use client";

import { useCallback, useEffect, useState } from "react";
import { orderApi } from "@/api/orderApi";
import {
  OrderListItem,
  ORDER_STATUS_OPTIONS,
  statusLabelToValue,
} from "@/types/order";

const PAGE_SIZE = 10;

export default function AdminOrderListPage() {
  const [orders, setOrders] = useState<OrderListItem[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<{ code: number; message: string } | null>(
    null
  );

  // 지금 펼쳐져 있는 주문 id들 (여러 개 동시에 펼칠 수 있음)
  const [expandedIds, setExpandedIds] = useState<Set<number>>(new Set());

  const [updatingStatusId, setUpdatingStatusId] = useState<number | null>(
    null
  );
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const fetchPage = useCallback(async (targetPage: number) => {
    setLoading(true);
    setError(null);

    try {
      const res = await orderApi.getList(targetPage, PAGE_SIZE);
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
  }, []);

  useEffect(() => {
    fetchPage(0);
  }, [fetchPage]);

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

  const handleStatusChange = async (orderId: number, newStatus: string) => {
    setUpdatingStatusId(orderId);
    setError(null);

    try {
      await orderApi.updateStatus(orderId, newStatus);
      // 응답에 목록 전체가 안 와서, 화면에서 해당 주문 status만 직접 갱신
      const label =
        ORDER_STATUS_OPTIONS.find((o) => o.value === newStatus)?.label ??
        newStatus;
      setOrders((prev) =>
        prev.map((o) =>
          o.orderId === orderId ? { ...o, status: label } : o
        )
      );
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
      setUpdatingStatusId(null);
    }
  };

  const handleDelete = async (orderId: number) => {
    if (!window.confirm(`주문번호 ${orderId}을(를) 삭제하시겠습니까?`)) return;

    setDeletingId(orderId);
    setError(null);

    try {
      await orderApi.delete(orderId);
      await fetchPage(page); // 삭제 후 같은 페이지 다시 불러오기
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

  return (
    <div className="mx-auto max-w-2xl px-8 py-10">
      <h1 className="mb-8 text-2xl font-bold text-black">주문 관리</h1>

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

      {!loading && !error && orders.length === 0 && (
        <p className="text-gray-500">주문이 없습니다.</p>
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
                  {/* (버튼/셀렉트는 안에서 stopPropagation으로 따로 처리) */}
                  <div
                    onClick={() => toggleExpand(order.orderId)}
                    className="flex w-full cursor-pointer items-center justify-between px-4 py-3"
                  >
                    <div>
                      <p className="font-medium text-black">
                        주문번호 {order.orderId} · {order.email}
                      </p>
                      <p className="text-xs text-gray-500">
                        {new Date(order.createdAt).toLocaleString()} · 상품{" "}
                        {order.items.length}종
                      </p>
                    </div>
                    <div
                      className="flex items-center gap-2"
                      onClick={(e) => e.stopPropagation()}
                    >
                      <select
                        value={statusLabelToValue(order.status)}
                        onChange={(e) =>
                          handleStatusChange(order.orderId, e.target.value)
                        }
                        disabled={updatingStatusId === order.orderId}
                        className="rounded border border-gray-200 px-2 py-1 text-sm text-black"
                      >
                        {ORDER_STATUS_OPTIONS.map((opt) => (
                          <option key={opt.value} value={opt.value}>
                            {opt.label}
                          </option>
                        ))}
                      </select>
                      <button
                        onClick={() => handleDelete(order.orderId)}
                        disabled={deletingId === order.orderId}
                        className="rounded bg-red-50 px-2 py-1 text-xs font-medium text-red-600 hover:bg-red-100 disabled:opacity-50"
                      >
                        {deletingId === order.orderId ? "삭제 중..." : "삭제"}
                      </button>
                      <span
                        className="cursor-pointer text-gray-400"
                        onClick={() => toggleExpand(order.orderId)}
                      >
                        {isExpanded ? "▲" : "▼"}
                      </span>
                    </div>
                  </div>

                  {/* 펼쳐졌을 때만 orderItem 배열 표시 */}
                  {isExpanded && (
                    <div className="border-t border-gray-100 px-4 py-3">
                      <div className="mb-3 flex flex-col gap-2">
                        {order.items.map((item) => (
                          <div
                            key={item.productId}
                            className="flex justify-between text-sm text-black"
                          >
                            <span>
                              {item.title} x {item.quantity}
                            </span>
                            <span>
                              {(item.price * item.quantity).toLocaleString()}원
                            </span>
                          </div>
                        ))}
                      </div>
                      <p className="text-xs text-gray-500">
                        배송지: {order.addressLine1} {order.addressLine2} (
                        {order.postalCode})
                      </p>
                    </div>
                  )}
                </div>
              );
            })}
          </div>

          {/* 페이지 이동 */}
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
