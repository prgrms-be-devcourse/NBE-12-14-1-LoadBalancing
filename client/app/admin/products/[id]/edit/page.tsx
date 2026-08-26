"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { productApi } from "@/api/productApi";
import { imageApi } from "@/api/imageApi";

export default function AdminProductEditPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const productId = Number(params.id);

  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [price, setPrice] = useState("");
  const [stock, setStock] = useState("");

  // 기존 이미지 URL (그대로 유지할 경우 이걸 씀)
  const [currentThumbnail, setCurrentThumbnail] = useState("");
  const [currentImgUrls, setCurrentImgUrls] = useState<string[]>([]);

  // 새로 고를 파일 (선택 안 하면 기존 걸 그대로 씀)
  const [newThumbnailFile, setNewThumbnailFile] = useState<File | null>(null);
  const [newImgFiles, setNewImgFiles] = useState<File[]>([]);

  // 새로 고른 파일의 미리보기용 blob URL (실제 업로드 전, 로컬에서만 보여주는 용도)
  const [thumbnailPreview, setThumbnailPreview] = useState<string | null>(null);
  const [imgPreviews, setImgPreviews] = useState<string[]>([]);

  useEffect(() => {
    if (!newThumbnailFile) {
      setThumbnailPreview(null);
      return;
    }
    const url = URL.createObjectURL(newThumbnailFile);
    setThumbnailPreview(url);
    return () => URL.revokeObjectURL(url); // 메모리 누수 방지, 파일 바뀌거나 언마운트되면 정리
  }, [newThumbnailFile]);

  useEffect(() => {
    if (newImgFiles.length === 0) {
      setImgPreviews([]);
      return;
    }
    const urls = newImgFiles.map((file) => URL.createObjectURL(file));
    setImgPreviews(urls);
    return () => urls.forEach((url) => URL.revokeObjectURL(url));
  }, [newImgFiles]);

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<{ code: number; message: string } | null>(
    null
  );

  // 진입 시 기존 상품 정보 불러와서 폼에 채워넣기
  useEffect(() => {
    setLoading(true);
    setNotFound(false);

    productApi
      .getDetail(productId)
      .then((data) => {
        setTitle(data.title);
        setDescription(data.description);
        setPrice(String(data.price));
        setStock(String(data.stock));
        setCurrentThumbnail(data.thumbnail);
        setCurrentImgUrls(data.imgs.map((img) => img.url));
      })
      .catch(() => setNotFound(true))
      .finally(() => setLoading(false));
  }, [productId]);

  const handleSubmit = async () => {
    if (submitting) return;
    setSubmitting(true);
    setError(null);

    try {
      // 새 파일을 골랐으면 업로드해서 URL로 바꾸고, 안 골랐으면 기존 URL 그대로 사용
      const thumbnailUrl = newThumbnailFile
        ? await imageApi.upload(newThumbnailFile)
        : currentThumbnail;

      const imageUrls =
        newImgFiles.length > 0
          ? await Promise.all(newImgFiles.map((file) => imageApi.upload(file)))
          : currentImgUrls;

      await productApi.update(productId, {
        title,
        description,
        price: Number(price),
        stock: Number(stock),
        thumbnail: thumbnailUrl,
        imageUrls,
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

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-gray-200 border-t-gray-800" />
      </div>
    );
  }

  if (notFound) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <p className="text-gray-500">해당 상품을 찾을 수 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-2xl px-8 py-10">
      <h1 className="mb-8 text-2xl font-bold text-black">상품 수정</h1>

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
            썸네일 (안 고르면 기존 이미지 유지)
          </label>
          {thumbnailPreview ? (
            <img
              src={thumbnailPreview}
              alt="새로 고른 썸네일 미리보기"
              className="mb-2 h-20 w-20 rounded-lg object-cover"
            />
          ) : (
            currentThumbnail && (
              <img
                src={currentThumbnail}
                alt="현재 썸네일"
                className="mb-2 h-20 w-20 rounded-lg object-cover"
              />
            )
          )}
          <input
            type="file"
            accept="image/*"
            onChange={(e) => setNewThumbnailFile(e.target.files?.[0] ?? null)}
          />
        </div>

        <div>
          <label className="mb-1 block text-sm text-gray-500">
            첨부사진 (새로 고르면 기존 사진 전체를 대체함)
          </label>
          {imgPreviews.length > 0 ? (
            <div className="mb-2 flex gap-2">
              {imgPreviews.map((url) => (
                <img
                  key={url}
                  src={url}
                  alt="새로 고른 사진 미리보기"
                  className="h-16 w-16 rounded-lg object-cover"
                />
              ))}
            </div>
          ) : (
            currentImgUrls.length > 0 && (
              <div className="mb-2 flex gap-2">
                {currentImgUrls.map((url) => (
                  <img
                    key={url}
                    src={url}
                    alt=""
                    className="h-16 w-16 rounded-lg object-cover"
                  />
                ))}
              </div>
            )
          )}
          <input
            type="file"
            accept="image/*"
            multiple
            onChange={(e) =>
              setNewImgFiles(e.target.files ? Array.from(e.target.files) : [])
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
        {submitting ? "수정 중..." : "수정 완료"}
      </button>
    </div>
  );
}
