"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { qnaApi } from "@/api/qnaApi";

export default function QnaWritePage() {
  const router = useRouter();

  const [email, setEmail] = useState("");
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async () => {
    if (submitting) return;
    if (!email.trim() || !title.trim() || !content.trim()) {
      setError("이메일, 제목, 내용을 모두 입력해주세요.");
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      await qnaApi.create({
        email: email.trim(),
        title: title.trim(),
        content: content.trim(),
      });
      // 등록하자마자 그 이메일로 바로 조회된 상태로 넘어가게 email을 쿼리로 실어보냄
      router.push(`/qna?email=${encodeURIComponent(email.trim())}`);
    } catch (e) {
      const axiosError = e as {
        response?: { data?: { code?: number; message?: string } };
      };
      setError(
        axiosError.response?.data?.message ?? "문의 등록에 실패했습니다."
      );
      setSubmitting(false);
    }
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
      <h1 className="text-headline-md mb-8 font-bold text-black">
        문의 작성
      </h1>

      <div className="flex flex-col gap-3">
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
          rows={8}
          className="text-body-md resize-none rounded-lg border border-gray-200 px-4 py-3 text-black"
        />

        {error && (
          <div className="rounded-lg bg-red-50 p-3 text-sm text-red-600">
            {error}
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
      </div>
    </div>
  );
}
