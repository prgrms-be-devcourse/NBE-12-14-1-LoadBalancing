package com.loadbalancing.kiosk.global.websocket;

import com.loadbalancing.kiosk.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

/*
    STOMP CONNECT 프레임을 가로채서 토큰을 검증하는 인터셉터.

    /ws 핸드셰이크(HTTP 레벨)에서 못 막는 이유: 브라우저 네이티브 WebSocket API는 핸드셰이크 요청에
    커스텀 헤더(Authorization)를 실을 수 없음. 그래서 SecurityConfig에서는 /ws를 permitAll로 열어두고,
    대신 그 위에서 오가는 STOMP 프로토콜의 CONNECT 프레임 헤더(클라이언트가 stompjs connectHeaders로
    직접 실어 보낼 수 있음)에 토큰을 담아 여기서 검증한다.
*/
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = resolveToken(accessor.getFirstNativeHeader("Authorization"));

            if (token == null || !jwtProvider.validateToken(token)) {
                // 여기서 예외를 던지면 CONNECT가 거부되고 클라이언트는 ERROR 프레임을 받으며 연결이 끊긴다.
                throw new MessagingException("웹소켓 인증에 실패했습니다. 유효한 토큰이 필요합니다.");
            }

            String adminId = jwtProvider.getAdminId(token);
            accessor.setUser((Principal) () -> adminId);
        }

        return message;
    }

    private String resolveToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
