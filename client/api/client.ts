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

// 토큰이 있어도 만료/무효면 서버가 401을 줌 - 그동안은 각 페이지가 그냥 "[401] 로그인이 필요합니다"를
// 화면에 에러 배너로 띄우기만 하고 끝이라, 로그인 페이지로 안 넘어가고 계속 그 상태로 남아있는 게
// 불편하다는 피드백이 있었음. 여기서 한 번에 잡아서 토큰 지우고 로그인 페이지로 보내버림
// (고객용 API는 전부 /auth/**라 원래 401이 날 일이 없어서, 관리자 API에만 해당됨)
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (
      typeof window !== "undefined" &&
      error.response?.status === 401 &&
      window.location.pathname !== "/admin/login"
    ) {
      localStorage.removeItem("admin_token");
      window.location.href = "/admin/login";
    }
    return Promise.reject(error);
  }
);
