package com.loadbalancing.kiosk.global;

import com.loadbalancing.kiosk.global.jwt.JwtAuthenticationFilter;
import com.loadbalancing.kiosk.global.security.ApiAccessDeniedHandler;
import com.loadbalancing.kiosk.global.security.ApiAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;
    private final ApiAccessDeniedHandler apiAccessDeniedHandler;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfiguratonSource()))
            //세션을 사용하지 않는다는 명시
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                    // 로그인 없이 되는 고객용/공개 API + 로그인 자체 (컨트롤러 컨벤션상 경로에 "auth"가 들어감)
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    // 웹소켓 핸드셰이크(HTTP GET /ws)는 여기서 막지 않음. 브라우저 네이티브 WebSocket은
                    // 핸드셰이크 요청에 커스텀 헤더(Authorization)를 못 실어서 여기선 검증이 불가능하고,
                    // 대신 STOMP CONNECT 프레임 헤더를 StompAuthChannelInterceptor에서 별도로 검증함
                    .requestMatchers("/ws/**").permitAll()
                    // Swagger UI/OpenAPI 문서 자체는 공개 (문서를 보는 것과, 문서 안에서 "Authorize"로
                    // 토큰 넣고 관리자 API를 실제로 호출하는 건 별개)
                    .requestMatchers(
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html"
                    ).permitAll()
                    // 나머지(관리자 API 등)는 로그인(유효한 JWT) 필요
                    .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(apiAuthenticationEntryPoint) // 토큰 없음/무효 -> 401
                    .accessDeniedHandler(apiAccessDeniedHandler) // 로그인은 했는데 권한 부족 -> 403
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfiguratonSource(){
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT","DELETE", "PATCH", "OPTIONS"));

        config.setAllowedHeaders(List.of("*"));

        config.setExposedHeaders(List.of("Authorization", "Set-Cookie"));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // JwtAuthenticationFilter가 @Component라서 Spring Boot가 기본적으로 이 필터를
    // 서블릿 컨테이너에도 자동 등록해버리는데(Security 체인과는 별개, 순서 보장도 안 됨),
    // 위에서 addFilterBefore로 Security 체인 안에 직접 등록했으니 중복 실행을 막기 위해 꺼둔다.
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean =
                new FilterRegistrationBean<>(jwtAuthenticationFilter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }
}
