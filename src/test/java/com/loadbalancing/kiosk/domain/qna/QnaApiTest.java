package com.loadbalancing.kiosk.domain.qna;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 고객용(비로그인) QnA API 테스트. 답변 등록(PATCH .../answer)은 원래 관리자 전용 액션인데
// 아직 /auth(공개) 경로 밑에 있는 알려진 이슈라, 여기서는 그 상태 그대로 테스트함.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QnaApiTest {

    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    private Long createQna(String email, String title) throws Exception {
        Map<String, Object> request = Map.of(
                "email", email,
                "title", title,
                "content", "문의 내용입니다."
        );

        String body = mockMvc.perform(post("/api/v1/auth/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).path("data").path("id").asLong();
    }

    @Test
    void 문의를_등록하면_201과_함께_생성된_정보를_반환한다() throws Exception {
        Map<String, Object> request = Map.of(
                "email", "customer@example.com",
                "title", "배송 문의",
                "content", "언제 오나요?"
        );

        mockMvc.perform(post("/api/v1/auth/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("배송 문의"))
                .andExpect(jsonPath("$.data.answered").value(false));
    }

    @Test
    void 필수값이_비어있으면_400이_난다() throws Exception {
        Map<String, Object> request = Map.of(
                "email", "customer@example.com",
                "title", "",
                "content", "내용"
        );

        mockMvc.perform(post("/api/v1/auth/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void email_없이_목록_조회하면_전체가_나온다() throws Exception {
        createQna("a@example.com", "문의1");
        createQna("b@example.com", "문의2");

        mockMvc.perform(get("/api/v1/auth/qna"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    void email과_함께_목록_조회하면_그_이메일_것만_나온다() throws Exception {
        createQna("filter-target@example.com", "내 문의");
        createQna("someone-else@example.com", "다른 사람 문의");

        mockMvc.perform(get("/api/v1/auth/qna").param("email", "filter-target@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].email").value("filter-target@example.com"));
    }

    @Test
    void 문의한적_없는_이메일로_조회하면_빈_목록이_나온다() throws Exception {
        mockMvc.perform(get("/api/v1/auth/qna").param("email", "nobody@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0));
    }

    @Test
    void 상세조회하면_해당_문의가_나온다() throws Exception {
        Long id = createQna("detail-test@example.com", "상세조회용 문의");

        mockMvc.perform(get("/api/v1/auth/qna/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("상세조회용 문의"));
    }

    @Test
    void 존재하지_않는_문의를_조회하면_404가_난다() throws Exception {
        mockMvc.perform(get("/api/v1/auth/qna/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 답변을_등록하면_answered가_true로_바뀐다() throws Exception {
        Long id = createQna("answer-test@example.com", "답변 필요");

        mockMvc.perform(patch("/api/v1/auth/qna/" + id + "/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("answer", "곧 발송됩니다."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answered").value(true))
                .andExpect(jsonPath("$.data.answer").value("곧 발송됩니다."));
    }
}
