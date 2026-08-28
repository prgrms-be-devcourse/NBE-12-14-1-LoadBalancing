package com.loadbalancing.kiosk.global.websocket;

import com.loadbalancing.kiosk.global.jwt.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// STOMP CONNECT 프레임 인증 인터셉터 단위테스트.
// 실제 웹소켓 연결까지 안 열고, "CONNECT 프레임 + 헤더"만 직접 만들어서 preSend()에 넣어보는 방식으로 검증.
class StompAuthChannelInterceptorTest {

    private final JwtProvider jwtProvider = mock(JwtProvider.class);
    private final StompAuthChannelInterceptor interceptor =
            new StompAuthChannelInterceptor(jwtProvider);
    private final MessageChannel channel = mock(MessageChannel.class);

    private Message<byte[]> connectMessageWithAuthHeader(String authHeaderValue) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (authHeaderValue != null) {
            accessor.setNativeHeader("Authorization", authHeaderValue);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    void 유효한_토큰이면_CONNECT를_통과시키고_유저를_세팅한다() {
        when(jwtProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtProvider.getAdminId("valid-token")).thenReturn("admin01");

        Message<byte[]> message = connectMessageWithAuthHeader("Bearer valid-token");

        Message<?> result = interceptor.preSend(message, channel);

        StompHeaderAccessor resultAccessor =
                StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNotNull();
        assertThat(resultAccessor.getUser().getName()).isEqualTo("admin01");
    }

    @Test
    void 토큰이_없으면_CONNECT를_거부한다() {
        Message<byte[]> message = connectMessageWithAuthHeader(null);

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void 유효하지_않은_토큰이면_CONNECT를_거부한다() {
        when(jwtProvider.validateToken("bad-token")).thenReturn(false);

        Message<byte[]> message = connectMessageWithAuthHeader("Bearer bad-token");

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void CONNECT가_아닌_프레임은_토큰_검증_없이_그냥_통과한다() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setLeaveMutable(true);
        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, channel);

        assertThat(result).isSameAs(message);
    }
}
