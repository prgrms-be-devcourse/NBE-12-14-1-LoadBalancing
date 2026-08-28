import { ProductInfo } from "./product";

// AdminRequest.LoginRequest (백엔드)
export interface LoginRequest {
  adminId: string;
  password: string;
}

// AdminResponse.LoginResponse (백엔드)
export interface LoginResponse {
  token: string;
}

// OrderStatusCountResponse (백엔드) - 주문 상태별 개수
export interface OrderStatusCount {
  status: string; // OrderStatus enum 이름 (예: "ORDER_RECEIVED")
  description: string; // 한글 설명 (예: "주문접수") - 백엔드가 이미 변환해서 내려줌
  count: number;
}

// AdminDashboardResponse (백엔드)
// 기준: 오후 2시 컷오프 (주문 생성 로직이랑 동일) - 일간은 "오늘 오후 2시 ~ 내일 오후 2시" 식
export interface AdminDashboardResponse {
  // 상품/재고
  totalProductCount: number;
  outOfStockProducts: ProductInfo[];
  lowStockProducts: ProductInfo[];
  recentProducts: ProductInfo[];

  // 매출
  dailyTotalSales: number;
  weeklyTotalSales: number;
  monthlyTotalSales: number;

  // 주문건수 (취소 제외)
  dailyOrderCount: number;
  weeklyOrderCount: number;
  monthlyOrderCount: number;

  // 평균 주문금액
  dailyAverageOrderAmount: number;
  weeklyAverageOrderAmount: number;
  monthlyAverageOrderAmount: number;

  // 주문 상태별 개수
  orderStatusCounts: OrderStatusCount[];
}
