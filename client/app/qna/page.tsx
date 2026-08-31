"use client";

import { Suspense, useCallback, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { qnaApi } from "@/api/qnaApi";
import { QnaInfo } from "@/types/qna";
import Icon from "@/components/Icon";

const PAGE_SIZE = 10;

// useSearchParams()를 쓰는 컴포넌트는 Suspense로 감싸야 함 (안 그러면 빌드 시 에러남) -
// /qna/new에서 등록 후 ?email=...로 돌아올 때 그 값을 읽으려고 씀
export default function QnaPage() {
  return (
    <Suspense fallback={null}>
      <QnaPageContent />
    </Suspense>
  );
}

function QnaPageContent() {
  const router = useRouter();
  const searchParams = useSearchParams();

  // 답변 확인은 이메일을 입력해야 볼 수 있게 함 (/orders 페이지랑 같은 패턴).
  // lookedUpEmail이 null이면 아직 조회 전 - 목록/답변은 안 보여줌
  const [lookupEmail, setLookupEmail] = useState("");
  const [lookedUpEmail, setLookedUpEmail] = useState<string | null>(null);

  const [qnas, setQnas] = useState<QnaInfo[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [lookupError, setLookupError] = useState<string | null>(null);

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

  // /qna/new에서 문의 등록하고 돌아올 때 ?email=...로 넘어오면, 입력 없이 바로 그 이메일로 조회함
  useEffect(() => {
    const emailFromQuery = searchParams.get("email");
    if (emailFromQuery) {
      setLookupEmail(emailFromQuery);
      setLookedUpEmail(emailFromQuery);
      fetchPage(0, emailFromQuery);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleLookup = () => {
    if (!lookupEmail.trim()) {
      setLookupError("이메일을 입력해주세요.");
      return;
    }
    setLookedUpEmail(lookupEmail.trim());
    fetchPage(0, lookupEmail.trim());
  };

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
      <div className="mb-8 flex items-center justify-between">
        <h1 className="text-headline-md font-bold text-black">문의하기</h1>
        <button
          onClick={() => router.push("/qna/new")}
          className="flex items-center gap-1 rounded-lg bg-black px-4 py-2 text-label-sm font-bold text-white"
        >
          <Icon name="add" className="text-lg" />
          문의 작성
        </button>
      </div>

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
                {qnas.map((qna) => (
                  <button
                    key={qna.id}
                    onClick={() => router.push(`/qna/${qna.id}`)}
                    className="flex w-full items-center justify-between rounded-lg border border-gray-200 px-4 py-3 text-left hover:border-black"
                  >
                    <div>
                      <p className="font-medium text-black">{qna.title}</p>
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
                      <Icon name="chevron_right" className="text-gray-400" />
                    </div>
                  </button>
                ))}
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
    </div>
  );
}
