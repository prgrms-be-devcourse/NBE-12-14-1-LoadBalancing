package com.loadbalancing.kiosk.domain.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 고객용(비로그인) 상품 조회 API 테스트.
// DataInitializer가 test 프로필에서도 돌아서, 여기 나오는 4개 상품(예가체프/콜롬비아/케냐/브라질)은
// 그 시딩 데이터 그대로임 - id는 생성 순서대로 1~4 (예가체프=1 ... 브라질=4)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // 각 테스트가 끝나면 롤백 -> 테스트끼리 서로 영향 안 줌 (초기 시딩 데이터는 유지됨)
class ProductApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void 상품_목록_조회하면_초기데이터_4개가_최신순으로_나온다() throws Exception {
        mockMvc.perform(get("/api/v1/auth/product/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(4))
                // @PageableDefault(sort = "id", direction = DESC) 이므로 가장 나중에 생성된(id=4) 브라질이 맨 앞
                .andExpect(jsonPath("$.data.content[0].title").value("브라질 산토스"))
                .andExpect(jsonPath("$.data.content[3].title").value("에티오피아 예가체프"));
    }

    @Test
    void 키워드로_검색하면_제목에_포함된_상품만_나온다() throws Exception {
        mockMvc.perform(get("/api/v1/auth/product/list").param("keyword", "케냐"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("케냐 AA"));
    }

    @Test
    void 검색결과가_없으면_빈_목록을_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/auth/product/list").param("keyword", "존재하지않는상품명"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0));
    }

    @Test
    void 상품_상세_조회하면_이미지_3장을_포함해서_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/auth/product/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("에티오피아 예가체프"))
                .andExpect(jsonPath("$.data.price").value(18000))
                .andExpect(jsonPath("$.data.imgs.length()").value(3));
    }

    @Test
    void 존재하지_않는_상품을_조회하면_404가_난다() throws Exception {
        mockMvc.perform(get("/api/v1/auth/product/detail/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404));
    }
}
