import axios from "axios";

// 백엔드랑 통신하는 모든 요청이 이 인스턴스 하나를 거쳐가게 함
export const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
});

// 요청 나갈 때마다 자동으로 실행됨: 로그인 토큰이 저장되어 있으면 헤더에 자동으로 붙여줌
// (페이지마다 매번 토큰 붙이는 코드 안 짜도 되게 하려고 여기서 공통 처리)
apiClient.interceptors.request.use((config) => {
  if (typeof window !== "undefined") {
    // localStorage는 브라우저에서만 있는 거라, 서버에서 렌더링될 때는 건드리면 안 됨 (그래서 window 체크)
    const token = localStorage.getItem("admin_token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});
