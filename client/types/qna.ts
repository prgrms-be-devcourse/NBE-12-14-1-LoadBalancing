// QnaRequest.QnaCreateRequest (백엔드)
export interface QnaCreateRequest {
  email: string;
  title: string;
  content: string;
}

// QnaRequest.QnaAnswerRequest (백엔드)
export interface QnaAnswerRequest {
  answer: string;
}

// QnaResponse.QnaInfo (백엔드) - 목록 조회에 email 필터가 없어서(GET /api/v1/auth/qna),
// "내 문의만" 보는 게 아니라 게시판처럼 전체 문의가 다 보이는 구조
export interface QnaInfo {
  id: number;
  email: string;
  title: string;
  content: string;
  answer: string | null;
  answered: boolean;
  createdAt: string;
}
