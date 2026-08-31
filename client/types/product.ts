export interface ProductImgInfo {
  id: number;
  url: string;
}

// 백엔드 ProductRequest랑 1:1로 맞춘 타입 (상품 생성용)
export interface ProductCreateRequest {
  title: string;
  description: string;
  price: number;
  stock: number;
  thumbnail: string;
  imgs: string[];
}

// ProductResponse.ProductInfo (백엔드)랑 1:1로 맞춘 타입
export interface ProductInfo {
  id: number;
  title: string;
  description: string;
  price: number;
  stock: number;
  thumbnail: string;
  imgs: ProductImgInfo[];
  createdAt: string; // LocalDateTime은 JSON으로 오면 문자열(ISO 형식)이라 Date 아니라 string
  updatedAt: string;
}

// 백엔드 AdminProductRequest.UpdateRequest랑 1:1로 맞춘 타입 (상품 수정용)
// 필드명이 create랑 다름 주의: imgs가 아니라 imageUrls
export interface ProductUpdateRequest {
  title: string;
  description: string;
  price: number;
  stock: number;
  thumbnail: string;
  imageUrls: string[];
}

// 백엔드 AdminProductResponse랑 1:1로 맞춘 타입 (수정 응답용)
export interface AdminProductInfo {
  id: number;
  title: string;
  description: string;
  price: number;
  stock: number;
  thumbnail: string;
  images: ProductImgInfo[];
}
