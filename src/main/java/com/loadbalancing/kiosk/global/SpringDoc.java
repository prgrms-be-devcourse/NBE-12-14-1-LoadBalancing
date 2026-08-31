package com.loadbalancing.kiosk.global;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// bearerAuth 스킴을 정의해두면 Swagger UI 우측 상단에 "Authorize" 버튼이 생겨서,
// 로그인으로 받은 JWT를 한 번만 넣어두면 그 뒤로 "Try it out"할 때마다 Authorization 헤더가 자동으로 붙는다.
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "API 서버", version = "beta", description = "API 서버 문서입니다."),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SpringDoc {
    @Bean
    public GroupedOpenApi groupApiV1() {
        return GroupedOpenApi.builder()
                .group("apiV1")
                .pathsToMatch("/api/**")
                .build();
    }
}