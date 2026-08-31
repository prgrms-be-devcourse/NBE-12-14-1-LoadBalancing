"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { qnaApi } from "@/api/qnaApi";
import { QnaInfo } from "@/types/qna";
import Icon from "@/components/Icon";

export default function QnaDetailPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();

  const [qna, setQna] = useState<QnaInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    const id = Number(params.id);
    setLoading(true);
    setNotFound(false);

    qnaApi
      .getDetail(id)
      .then((data) => setQna(data))
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

  if (notFound || !qna) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-4">
        <p className="text-gray-500">해당 문의를 찾을 수 없습니다.</p>
        <button
          onClick={() => router.back()}
          className="rounded-lg border-2 border-black px-5 py-2 font-bold text-black"
        >
          돌아가기
        </button>
      </div>
    );
  }

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
      <button
        onClick={() => router.back()}
        className="mb-8 flex items-center gap-1 text-body-md font-medium text-gray-500 hover:text-black"
      >
        <Icon name="arrow_back" className="text-lg" />
        목록으로
      </button>

      <div className="mb-6 flex items-start justify-between gap-4">
        <div>
          <h1 className="text-headline-md font-bold text-black">
            {qna.title}
          </h1>
          <p className="mt-2 text-sm text-gray-500">
            {qna.email} · {new Date(qna.createdAt).toLocaleString()}
          </p>
        </div>
        <span
          className={`text-label-sm shrink-0 inline-flex items-center rounded px-2 py-1 font-bold uppercase ${
            qna.answered ? "bg-black text-white" : "bg-gray-100 text-gray-500"
          }`}
        >
          {qna.answered ? "답변완료" : "답변대기"}
        </span>
      </div>

      <div className="rounded-lg border border-gray-200 p-6">
        <p className="whitespace-pre-wrap text-body-md text-black">
          {qna.content}
        </p>
      </div>

      <div className="mt-4 rounded-lg bg-gray-50 p-6">
        <p className="text-label-sm mb-2 font-bold uppercase tracking-wide text-gray-400">
          답변
        </p>
        {qna.answered ? (
          <p className="whitespace-pre-wrap text-body-md text-black">
            {qna.answer}
          </p>
        ) : (
          <p className="text-sm text-gray-400">
            아직 답변이 등록되지 않았습니다.
          </p>
        )}
      </div>
      </div>
    </div>
  );
}
