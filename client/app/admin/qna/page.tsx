"use client";

import { useCallback, useEffect, useState } from "react";
import { qnaApi } from "@/api/qnaApi";
import { QnaInfo } from "@/types/qna";
import Icon from "@/components/Icon";

const PAGE_SIZE = 10;

export default function AdminQnaListPage() {
  const [qnas, setQnas] = useState<QnaInfo[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<{ code: number; message: string } | null>(
    null
  );

  const [expandedIds, setExpandedIds] = useState<Set<number>>(new Set());

  // 답변 입력 중인 문의 id -> 입력값
  const [answerDrafts, setAnswerDrafts] = useState<Record<number, string>>(
    {}
  );
  const [submittingId, setSubmittingId] = useState<number | null>(null);

  const fetchPage = useCallback(async (targetPage: number) => {
    setLoading(true);
    setError(null);

    try {
      const res = await qnaApi.getList(targetPage, PAGE_SIZE);
      setQnas(res.content);
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

  const toggleExpand = (id: number) => {
    setExpandedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  const handleAnswer = async (id: number) => {
    const answer = (answerDrafts[id] ?? "").trim();
    if (!answer) return;

    setSubmittingId(id);
    setError(null);

    try {
      const updated = await qnaApi.answer(id, { answer });
      // 응답으로 최신 문의(답변 포함)가 오니, 목록에서 해당 항목만 갱신
      setQnas((prev) => prev.map((q) => (q.id === id ? updated : q)));
      setAnswerDrafts((prev) => {
        const next = { ...prev };
        delete next[id];
        return next;
      });
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
      setSubmittingId(null);
    }
  };

  return (
    <div className="mx-auto w-[896px] px-8 py-10">

      <h1 className="text-headline-md mb-8 font-bold text-black">
        문의 관리
      </h1>

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

      {!loading && !error && qnas.length === 0 && (
        <p className="text-gray-500">등록된 문의가 없습니다.</p>
      )}

      {!loading && qnas.length > 0 && (
        <>
          <div className="flex flex-col gap-3">
            {qnas.map((qna) => {
              const isExpanded = expandedIds.has(qna.id);
              return (
                <div
                  key={qna.id}
                  className="rounded-lg border border-gray-200"
                >
                  <div
                    onClick={() => toggleExpand(qna.id)}
                    className="flex w-full cursor-pointer items-center justify-between px-4 py-3"
                  >
                    <div>
                      <p className="font-bold text-black">
                        {qna.title} · {qna.email}
                      </p>
                      <p className="text-label-sm mt-0.5 text-gray-500">
                        {new Date(qna.createdAt).toLocaleString()}
                      </p>
                    </div>
                    <div className="flex items-center gap-3">
                      <span
                        className={`text-label-sm inline-flex items-center rounded px-2 py-0.5 font-bold uppercase ${
                          qna.answered
                            ? "bg-black text-white"
                            : "bg-orange-50 text-orange-600"
                        }`}
                      >
                        {qna.answered ? "답변완료" : "답변대기"}
                      </span>
                      <Icon
                        name={isExpanded ? "expand_less" : "expand_more"}
                        className="text-gray-400"
                      />
                    </div>
                  </div>

                  <div
                    className={`grid transition-[grid-template-rows] duration-300 ease-in-out ${
                      isExpanded ? "grid-rows-[1fr]" : "grid-rows-[0fr]"
                    }`}
                  >
                    <div className="overflow-hidden">
                      <div className="flex flex-col gap-3 border-t border-gray-100 px-4 py-3">
                        <p className="whitespace-pre-wrap text-sm text-black">
                          {qna.content}
                        </p>

                        {qna.answered ? (
                          <div className="rounded-lg bg-gray-50 p-3">
                            <p className="text-label-sm mb-1 font-bold text-gray-500">
                              답변
                            </p>
                            <p className="whitespace-pre-wrap text-sm text-black">
                              {qna.answer}
                            </p>
                          </div>
                        ) : (
                          <div
                            className="flex flex-col gap-2"
                            onClick={(e) => e.stopPropagation()}
                          >
                            <textarea
                              placeholder="답변을 입력하세요"
                              value={answerDrafts[qna.id] ?? ""}
                              onChange={(e) =>
                                setAnswerDrafts((prev) => ({
                                  ...prev,
                                  [qna.id]: e.target.value,
                                }))
                              }
                              rows={3}
                              className="text-body-md resize-none rounded-lg border border-gray-200 px-3 py-2 text-black"
                            />
                            <button
                              onClick={() => handleAnswer(qna.id)}
                              disabled={
                                submittingId === qna.id ||
                                !(answerDrafts[qna.id] ?? "").trim()
                              }
                              className="self-end rounded bg-black px-4 py-2 text-label-sm font-bold text-white disabled:opacity-50"
                            >
                              {submittingId === qna.id
                                ? "등록 중..."
                                : "답변 등록"}
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

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
