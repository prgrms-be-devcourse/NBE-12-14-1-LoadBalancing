"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { productApi } from "@/api/productApi";
import { imageApi } from "@/api/imageApi";

export default function AdminProductCreatePage() {
  const router = useRouter();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [price, setPrice] = useState("");
  const [stock, setStock] = useState("");
  const [thumbnailFile, setThumbnailFile] = useState<File | null>(null);
  const [imgFiles, setImgFiles] = useState<File[]>([]);

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<{ code: number; message: string } | null>(
    null
  );

  const handleSubmit = async () => {
    if (submitting) return;

    if (!thumbnailFile) {
      setError({ code: 400, message: "썸네일을 등록해주세요." });
      return;
    }
    if (imgFiles.length === 0) {
      setError({ code: 400, message: "첨부사진을 등록해주세요." });
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      // 1. 썸네일/첨부사진 파일들을 먼저 각각 업로드해서 URL로 변환
      const thumbnailUrl = await imageApi.upload(thumbnailFile);
      const imgUrls = await Promise.all(
        imgFiles.map((file) => imageApi.upload(file))
      );

      // 2. URL만 넣어서 상품 생성 요청
      await productApi.create({
        title,
        description,
        price: Number(price),
        stock: Number(stock),
        thumbnail: thumbnailUrl,
        imgs: imgUrls,
      });

      router.push("/admin/products");
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
    <div className="mx-auto max-w-2xl px-8 py-10">
      <h1 className="mb-8 text-2xl font-bold text-black">상품 생성</h1>

      <div className="flex flex-col gap-4">
        <input
          type="text"
          placeholder="제목"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className="rounded-lg border border-gray-200 px-4 py-3 text-black"
        />
        <textarea
          placeholder="설명"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          rows={4}
          className="rounded-lg border border-gray-200 px-4 py-3 text-black"
        />
        <input
          type="number"
          placeholder="가격"
          value={price}
          onChange={(e) => setPrice(e.target.value)}
          className="rounded-lg border border-gray-200 px-4 py-3 text-black"
        />
        <input
          type="number"
          placeholder="재고"
          value={stock}
          onChange={(e) => setStock(e.target.value)}
          className="rounded-lg border border-gray-200 px-4 py-3 text-black"
        />

        <div>
          <label className="mb-1 block text-sm text-gray-500">
            썸네일 (1장)
          </label>
          <input
            type="file"
            accept="image/*"
            onChange={(e) => setThumbnailFile(e.target.files?.[0] ?? null)}
          />
        </div>

        <div>
          <label className="mb-1 block text-sm text-gray-500">
            첨부사진 (여러 장 선택 가능)
          </label>
          <input
            type="file"
            accept="image/*"
            multiple
            onChange={(e) =>
              setImgFiles(e.target.files ? Array.from(e.target.files) : [])
            }
          />
        </div>
      </div>

      {error && (
        <div className="mt-4 rounded-lg bg-red-50 p-4 text-sm text-red-600">
          [{error.code}] {error.message}
        </div>
      )}

      <button
        onClick={handleSubmit}
        disabled={submitting}
        className="mt-8 flex w-full items-center justify-center gap-2 rounded-lg bg-black py-4 font-medium text-white disabled:opacity-50"
      >
        {submitting && (
          <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
        )}
        {submitting ? "등록 중..." : "상품 등록"}
      </button>
    </div>
  );
}
