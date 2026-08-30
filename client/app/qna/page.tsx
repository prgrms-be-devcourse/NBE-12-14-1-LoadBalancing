"use client";

import { useCallback, useState } from "react";
import { qnaApi } from "@/api/qnaApi";
import { QnaInfo } from "@/types/qna";
import BackToHomeButton from "@/components/BackToHomeButton";
import Icon from "@/components/Icon";

const PAGE_SIZE = 10;

export default function QnaPage() {
  // 답변 확인은 이메일을 입력해야 볼 수 있게 함 (/orders 페이지랑 같은 패턴).
  // lookedUpEmail이 null이면 아직 조회 전 - 목록/답변은 안 보여줌
  const [lookupEmail, setLookupEmail] = useState("");
  const [lookedUpEmail, setLookedUpEmail] = useState<string | null>(null);

  const [qnas, setQnas] = useState<QnaInfo[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [lookupError, setLookupError] = useState<string | null>(null);

  // 지금 펼쳐져 있는 문의 id들
  const [expandedIds, setExpandedIds] = useState<Set<number>>(new Set());

  // 작성 폼 상태
  const [showForm, setShowForm] = useState(false);
  const [email, setEmail] = useState("");
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const fetchPage = useCallback(
    async (targetPage: number, targetEmail: string) => {
      setLoading(true);
      setLookupError(null);

      try {
        const res = await qnaApi.getList(targetPage, PAGE_SIZE, targetEmail);
        setQnas(res.content);
        setTotalPages(res.totalPages);
        setPage(res.number);
      } catch (e) {
        const axiosError = e as {
          response?: { data?: { code?: number; message?: string } };
        };
        setLookupError(
          axiosError.response?.data?.message ?? "조회에 실패했습니다."
        );
      } finally {
        setLoading(false);
      }
    },
    []
  );

  const handleLookup = () => {
    if (!lookupEmail.trim()) {
      setLookupError("이메일을 입력해주세요.");
      return;
    }
    setLookedUpEmail(lookupEmail.trim());
    fetchPage(0, lookupEmail.trim());
  };

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

  const handleSubmit = async () => {
    if (submitting) return;
    if (!email.trim() || !title.trim() || !content.trim()) {
      setSubmitError("이메일, 제목, 내용을 모두 입력해주세요.");
      return;
    }

    setSubmitting(true);
    setSubmitError(null);

    try {
      await qnaApi.create({
        email: email.trim(),
        title: title.trim(),
        content: content.trim(),
      });
      const submittedEmail = email.trim();
      setEmail("");
      setTitle("");
      setContent("");
      setShowForm(false);

      // 방금 문의 등록한 이메일로 바로 조회 화면까지 이어서 보여줌
      setLookupEmail(submittedEmail);
      setLookedUpEmail(submittedEmail);
      await fetchPage(0, submittedEmail);
    } catch (e) {
      const axiosError = e as {
        response?: { data?: { code?: number; message?: string } };
      };
      setSubmitError(
        axiosError.response?.data?.message ?? "문의 등록에 실패했습니다."
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto w-[896px] px-8 py-10">
      <BackToHomeButton />

      <div className="mb-8 flex items-center justify-between">
        <h1 className="text-headline-md font-bold text-black">문의하기</h1>
        <button
          onClick={() => setShowForm((prev) => !prev)}
          className="flex items-center gap-1 rounded-lg bg-black px-4 py-2 text-label-sm font-bold text-white"
        >
          <Icon name={showForm ? "close" : "add"} className="text-lg" />
          {showForm ? "취소" : "문의 작성"}
        </button>
      </div>

      {showForm && (
        <div className="mb-8 flex flex-col gap-3 rounded-lg border border-gray-200 p-5">
          <input
            type="email"
            placeholder="이메일"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="text-body-md rounded-lg border border-gray-200 px-4 py-3 text-black"
          />
          <input
            type="text"
            placeholder="제목"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="text-body-md rounded-lg border border-gray-200 px-4 py-3 text-black"
          />
          <textarea
            placeholder="문의 내용"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            rows={5}
            className="text-body-md resize-none rounded-lg border border-gray-200 px-4 py-3 text-black"
          />

          {submitError && (
            <div className="rounded-lg bg-red-50 p-3 text-sm text-red-600">
              {submitError}
            </div>
          )}

          <button
            onClick={handleSubmit}
            disabled={submitting}
            className="touch-target rounded-lg bg-black font-bold text-white disabled:opacity-50"
          >
            {submitting ? "등록 중..." : "등록하기"}
          </button>
        </div>
      )}

      {/* 답변 확인 - 이메일을 입력해야 그 이메일로 등록한 문의/답변만 볼 수 있음 */}
      <div className="mb-6 rounded-lg border border-gray-200 p-5">
        <h2 className="text-label-lg mb-3 font-bold text-black">
          내 문의 확인
        </h2>
        <div className="flex gap-2">
          <input
            type="email"
            placeholder="문의할 때 입력한 이메일"
            value={lookupEmail}
            onChange={(e) => setLookupEmail(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") handleLookup();
            }}
            className="text-body-md flex-1 rounded-lg border border-gray-200 px-4 py-3 text-black"
          />
          <button
            onClick={handleLookup}
            className="touch-target rounded-lg bg-black px-6 font-bold text-white"
          >
            조회
          </button>
        </div>
        {lookupError && (
          <p className="mt-2 text-sm text-red-600">{lookupError}</p>
        )}
      </div>

      {lookedUpEmail === null ? (
        <p className="text-center text-gray-400">
          이메일을 입력하면 내가 등록한 문의와 답변을 확인할 수 있어요.
        </p>
      ) : (
        <>
          {loading && (
            <div className="flex justify-center py-16">
              <div className="h-8 w-8 animate-spin rounded-full border-4 border-gray-200 border-t-gray-800" />
            </div>
          )}

          {!loading && !lookupError && qnas.length === 0 && (
            <p className="text-gray-500">
              {lookedUpEmail}로 등록된 문의가 없습니다.
            </p>
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
                      <button
                        onClick={() => toggleExpand(qna.id)}
                        className="flex w-full items-center justify-between px-4 py-3 text-left"
                      >
                        <div>
                          <p className="font-medium text-black">
                            {qna.title}
                          </p>
                          <p className="text-xs text-gray-500">
                            {new Date(qna.createdAt).toLocaleString()}
                          </p>
                        </div>
                        <div className="flex items-center gap-3">
                          <span
                            className={`text-label-sm inline-flex items-center rounded px-2 py-0.5 font-bold uppercase ${
                              qna.answered
                                ? "bg-black text-white"
                                : "bg-gray-100 text-gray-500"
                            }`}
                          >
                            {qna.answered ? "답변완료" : "답변대기"}
                          </span>
                          <Icon
                            name={isExpanded ? "expand_less" : "expand_more"}
                            className="text-gray-400"
                          />
                        </div>
                      </button>

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
                              <p className="text-xs text-gray-400">
                                아직 답변이 등록되지 않았습니다.
                              </p>
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
                  onClick={() => fetchPage(page - 1, lookedUpEmail)}
                  disabled={page === 0}
                  className="rounded border border-gray-200 px-3 py-1 text-black disabled:opacity-30"
                >
                  이전
                </button>
                <span className="text-sm text-gray-500">
                  {page + 1} / {totalPages}
                </span>
                <button
                  onClick={() => fetchPage(page + 1, lookedUpEmail)}
                  disabled={page + 1 >= totalPages}
                  className="rounded border border-gray-200 px-3 py-1 text-black disabled:opacity-30"
                >
                  다음
                </button>
              </div>
            </>
          )}
        </>
      )}
    </div>
  );
}
