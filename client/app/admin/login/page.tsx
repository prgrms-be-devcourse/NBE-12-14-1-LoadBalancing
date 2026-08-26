"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { adminApi } from "@/api/adminApi";

export default function AdminLoginPage() {
  const router = useRouter();
  const [adminId, setAdminId] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<{ code: number; message: string } | null>(
    null
  );

  const handleSubmit = async () => {
    if (submitting) return;
    setSubmitting(true);
    setError(null);

    try {
      console.log("[로그인 요청 body]", { adminId, password }); // 확인용, 나중에 지워도 됨
      const data = await adminApi.login({ adminId, password });
      console.log("로그인 응답으로 받은 JWT:", data.token); // 확인용, 나중에 지워도 됨
      localStorage.setItem("admin_token", data.token); // api/client.ts가 이 키를 읽어서 Authorization 헤더에 자동으로 붙임
      router.push("/admin/orders");
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
      setSubmitting(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-white px-8">
      <div className="w-full max-w-sm">
        <h1 className="mb-8 text-2xl font-bold text-black">관리자 로그인</h1>

        <div className="flex flex-col gap-4">
          <input
            type="text"
            placeholder="아이디"
            value={adminId}
            onChange={(e) => setAdminId(e.target.value)}
            className="rounded-lg border border-gray-200 px-4 py-3 text-black"
          />
          <input
            type="password"
            placeholder="비밀번호"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") handleSubmit();
            }}
            className="rounded-lg border border-gray-200 px-4 py-3 text-black"
          />
        </div>

        {error && (
          <div className="mt-4 rounded-lg bg-red-50 p-4 text-sm text-red-600">
            [{error.code}] {error.message}
          </div>
        )}

        <button
          onClick={handleSubmit}
          disabled={submitting}
          className="mt-6 flex w-full items-center justify-center gap-2 rounded-lg bg-black py-3 font-medium text-white disabled:opacity-50"
        >
          {submitting && (
            <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
          )}
          {submitting ? "로그인 중..." : "로그인"}
        </button>
      </div>
    </div>
  );
}
