// AdminRequest.LoginRequest (백엔드)
export interface LoginRequest {
  adminId: string;
  password: string;
}

// AdminResponse.LoginResponse (백엔드)
export interface LoginResponse {
  token: string;
}
