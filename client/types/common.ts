// 백엔드 ApiResponse<T>랑 1:1로 맞춘 타입.
// 모든 API 응답이 이 모양으로 온다 (성공/실패 상관없이).
export interface ApiResponse<T> {
  success: boolean;
  code: number;
  message: string;
  data: T;
}

// Spring Data의 Page<T>를 JSON으로 내리면 이 모양으로 옴 (상품 목록 조회에서 씀)
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // 현재 페이지 (0부터 시작)
  size: number;
  last: boolean; // 이게 마지막 페이지인지 (무한스크롤 종료 판단용)
}
