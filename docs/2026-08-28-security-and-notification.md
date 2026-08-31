# 2026-08-28 백엔드 작업 정리 — 인증/보안, 웹소켓 알림 인증, 스웨거, 주문항목 삭제

작성자: Claude (요청자: 재환)
브랜치: `refactor/productSearch` (작업 당시 기준, 커밋은 아직 안 함)

## 개요

미뤄뒀던 API 인증(Security) 잠금을 실제로 적용하고, 그 여파로 막혀야 하는 부분들
(웹소켓, 스웨거, 테스트)을 같이 정리했다. 추가로 고객용 주문항목 삭제 기능에
필요한 DTO 필드 하나를 보강했다. 전부 이 세션 안에서 컴파일/테스트 통과까지 확인함
(`./gradlew test` 기준 43개 테스트 전부 성공).

내일(8/29) 재환님이 직접 한 번 더 검토하면서 필요하면 바꿀 예정이라, 각 변경의
"무엇을/왜"를 아래에 정리해둔다.

---

## 1. SecurityConfig — 인증 규칙을 실제로 적용

**파일**: `src/main/java/com/loadbalancing/kiosk/global/SecurityConfig.java`

### 문제였던 상태

```java
.authorizeHttpRequests(auth -> auth
        .anyRequest().permitAll() // <- 아직 임시고 추후 auth 적혀있는건 통과, 안적혀 있으면 로그인 된 상태만 가능
);
```

모든 API가 토큰 없이 그냥 열려있었음. `JwtAuthenticationFilter`가 토큰을 검증해서
`SecurityContextHolder`에 인증 정보를 넣어주긴 했지만, `authorizeHttpRequests`가
아무것도 막지 않으니 있으나 마나였음.

**추가로 발견한 문제**: `JwtAuthenticationFilter`가 `@Component`로만 등록돼 있고
`SecurityFilterChain`에 `addFilterBefore`로 명시적으로 물려있지 않았음. 이 상태로는
Spring Boot가 이 필터를 Security 체인과는 별개의 서블릿 필터로 자동 등록하는데,
그 실행 순서가 Security 체인보다 늦어질 수 있어서(기본 우선순위 최하위) 인증 정보가
`authorizeHttpRequests` 판단 시점에 아직 안 들어가 있을 수 있음. 지금까지는
`permitAll()`이라 이 문제가 드러나지 않았던 것.

### 바꾼 내용

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/v1/auth/**").permitAll()   // 컨트롤러 컨벤션상 "auth"가 붙은 경로 = 공개 API
        .requestMatchers("/ws/**").permitAll()             // 아래 3번 참고 - 인증은 STOMP 레벨에서 별도 처리
        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
        .anyRequest().authenticated()                       // 나머지(주로 /api/v1/admin/**)는 로그인 필요
)
.exceptionHandling(exception -> exception
        .authenticationEntryPoint(apiAuthenticationEntryPoint) // 토큰 없음/무효 -> 401
        .accessDeniedHandler(apiAccessDeniedHandler)           // 권한 부족 -> 403
)
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

- 매처는 기존 컨트롤러들의 `@RequestMapping` 컨벤션(`/api/v1/auth/*` = 공개,
  `/api/v1/admin/*` = 관리자)을 그대로 따름. 새 컨트롤러를 추가할 때도 이 컨벤션을
  지키면 별도로 SecurityConfig를 안 건드려도 됨.
- `JwtAuthenticationFilter`를 이제 `FilterRegistrationBean`으로 자동 등록되는 걸
  꺼두고(`setEnabled(false)`) `addFilterBefore`로만 등록되게 해서 중복 실행을 막음.

### 새로 만든 파일

- `global/security/ApiAuthenticationEntryPoint.java` — 401을 다른 API들과 같은
  `{success, code, message, data}` JSON 형식으로 응답. (커스텀 EntryPoint 없으면
  Spring Security 기본 동작(빈 403 등)이 나가서 형식이 안 맞았음)
- `global/security/ApiAccessDeniedHandler.java` — 403도 동일한 형식으로. 지금은
  역할이 `ROLE_ADMIN` 하나뿐이라 당장 걸릴 일은 없지만, 나중에 역할이 늘어날 때를
  대비해 미리 둠.

### 검토 포인트 (내일 확인용)

- `/api/v1/auth/**` 매처가 전부 맞는지 재검토 필요. 특히 `QnaController`의
  `PATCH /api/v1/auth/qna/{id}/answer`(관리자 답변 등록)가 `/auth` 밑에 있어서
  지금도 로그인 없이 호출 가능함 — 원래 관리자 전용 액션이라 `/admin/qna`로
  옮기는 게 맞아 보이는데, 오늘 작업 범위엔 안 넣어서 그대로 둠.
- CORS 허용 origin(`corsConfiguratonSource()`)과 웹소켓 허용 origin
  (`WebSocketConfig.registerStompEndpoints`)이 둘 다 `localhost:3000`/`5173`으로
  하드코딩돼 있음. 배포 도메인 정해지면 같이 추가해야 함.

---

## 2. 웹소켓 인증 — STOMP CONNECT 프레임 검증

**새 파일**: `global/websocket/StompAuthChannelInterceptor.java`
**수정 파일**: `global/websocket/WebSocketConfig.java`

### 왜 HTTP 레벨에서 안 막았는가

`/ws` 핸드셰이크는 HTTP GET 요청이긴 한데, 브라우저 네이티브 WebSocket API가
핸드셰이크 요청에 커스텀 헤더(`Authorization`)를 실을 수 없음. 그래서
`SecurityConfig`에서는 `/ws/**`를 permitAll로 열어두고, 그 위에서 오가는 STOMP
프로토콜의 CONNECT 프레임(이건 stompjs가 `connectHeaders`로 커스텀 헤더를 실어
보낼 수 있음)에서 인증하도록 함.

### 동작 방식

```java
@Override
public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
        String token = resolveToken(accessor.getFirstNativeHeader("Authorization"));
        if (token == null || !jwtProvider.validateToken(token)) {
            throw new MessagingException("웹소켓 인증에 실패했습니다. 유효한 토큰이 필요합니다.");
        }
        accessor.setUser((Principal) () -> jwtProvider.getAdminId(token));
    }
    return message;
}
```

- CONNECT 프레임에서만 토큰을 확인하고, 그 외 프레임(SUBSCRIBE 등)은 그냥 통과시킴.
- 여기서 예외를 던지면 stompjs 클라이언트가 `onStompError` 콜백으로 ERROR 프레임을
  받고 연결이 끊김.
- `WebSocketConfig.configureClientInboundChannel()`에 이 인터셉터를 등록해야
  실제로 동작함 — 등록만 빠뜨리면 인터셉터 클래스가 있어도 조용히 안 먹힘.

### 검토 포인트

- 지금은 토큰이 유효한지만 확인하고 별도 role 체크는 안 함(관리자 토큰이면
  전부 통과). ROLE 기반으로 더 세분화할 일이 생기면 `accessor.setUser()`에
  role 정보도 같이 실어야 함.
- 프론트에서 `@stomp/stompjs`의 `connectHeaders: { Authorization: "Bearer ..." }`로
  토큰을 실어 보내는 걸 전제로 함 (`client/hooks/useOrderNotifications.ts`).

---

## 3. Swagger — Authorize 버튼 추가

**파일**: `global/SpringDoc.java`

```java
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
public class SpringDoc { ... }
```

Swagger UI 우측 상단에 자물쇠 아이콘("Authorize")이 생김. 로그인 API로 받은 JWT를
`Bearer <토큰>` 형식 없이 토큰만 넣으면(swagger-ui가 알아서 `Bearer ` 붙여줌),
그 뒤로 "Try it out"할 때마다 `Authorization` 헤더가 자동으로 붙어서 관리자 API를
문서에서 바로 테스트할 수 있음.

### 겸사겸사 정리한 것

`build.gradle.kts`에 springdoc 의존성이 두 줄(3.0.2, 3.1.0) 중복으로 들어가 있던 것
발견해서 오래된 3.0.2 줄 제거함. 머지 과정에서 남은 잔재로 추정.

---

## 4. 주문항목 삭제용 `itemId` 필드 추가

**파일**: `domain/order/dto/OrderResponse.java`

`DELETE /api/v1/auth/order/{orderId}/items/{itemId}` 엔드포인트(고객이 자기 주문에서
항목 하나만 뺄 때 쓰는 API)는 이미 있었는데, 응답 DTO인 `OrderItemInfo`에 그 항목
자체의 PK가 없어서 프론트가 이 API를 호출할 방법이 없었음.

```java
public record OrderItemInfo(
    Long itemId,      // 추가: OrderItem 자신의 PK (productId와는 다름)
    Long productId,
    String title,
    int price,
    Long quantity
) {
    public static OrderItemInfo from(OrderItem orderItem) {
        return OrderItemInfo.builder()
            .itemId(orderItem.getId())
            .productId(orderItem.getProduct().getId())
            ...
    }
}
```

응답 필드 추가만 한 거라 기존 API 스펙을 깨지 않음(하위 호환).

---

## 5. 테스트

기존 admin 관련 테스트 3개가 `/api/v1/admin/**`를 토큰 없이 호출하고 있어서,
SecurityConfig 변경 직후 전부 401로 실패했음(15개). `JwtProvider`를 `@Autowired`
받아서 `"Bearer " + jwtProvider.generateToken("admin01")`을 헤더에 붙이는 방식으로
전부 고침.

- `AdminDashboardApiTest`, `AdminOrderApiTest`, `AdminProductApiTest` — 관리자 API
  호출부에 `Authorization` 헤더 추가
- `SecurityConfigApiTest`(신규) — 토큰 없이 관리자 API 호출 시 401, 잘못된 토큰도
  401, 유효한 토큰이면 통과, 공개 API는 토큰 없이도 통과 — 4종
- `StompAuthChannelInterceptorTest`(신규) — 실제 웹소켓 연결 없이 CONNECT 프레임을
  직접 만들어서 인터셉터에 넣어보는 단위테스트. 정상 토큰/토큰 없음/무효 토큰/
  CONNECT가 아닌 프레임 4종
- `OrderApiTest`에 항목 삭제 관련 테스트 3종 추가 — 항목 하나 삭제(주문은 유지),
  마지막 항목 삭제(주문도 같이 삭제됨, 기존 `OrderService.deleteOrderItem` 로직
  그대로), 존재하지 않는 항목 삭제 시 404

`./gradlew test` 기준 총 43개, 전부 성공.

---

## 오늘 건드리지 않은 것 (알고 있는 이슈)

- `QnaController.answer()`가 여전히 `/api/v1/auth/qna/{id}/answer`에 있음 —
  관리자 전용 액션인데 공개 경로에 있어서, 인증 없이 호출 가능한 상태 그대로임.
- `ProductService.getProductsList()`의 가격 검색은 이미 이전 세션에서 min/max
  중 하나만 넣어도 되게 고쳐놓음(오늘 작업 아님, 참고용으로만 기록).
- 실제 브라우저에서 로그인 → 웹소켓 연결 → 알림 수신까지 이어지는 흐름은
  서버를 안 띄운 상태라 직접 눈으로는 아직 확인 못 함. 내일 서버 켜서 확인 필요.
