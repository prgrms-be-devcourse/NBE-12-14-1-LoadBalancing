package com.loadbalancing.kiosk.domain.notification.discord;

import com.loadbalancing.kiosk.domain.notification.dto.DiscordWebhookRequest;
import com.loadbalancing.kiosk.domain.notification.dto.OrderCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.format.DateTimeFormatter;

// Discord Webhook API를 호출하여 주문 완료 메시지를 전송하는 클라이언트
@Component
@Slf4j
public class DiscordWebhookClient {

    // Discord에 표시할 주문 시간 형식
    private static final DateTimeFormatter ORDERED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일 HH:mm");

    private final RestClient restClient;
    private final boolean enabled;
    private final String webhookUrl;

    public DiscordWebhookClient(
            @Value("${notification.discord.enabled:false}")
            boolean enabled,
            @Value("${notification.discord.webhook-url:}")
            String webhookUrl
    ) {
        this.restClient = RestClient.create();
        this.enabled = enabled;
        this.webhookUrl = webhookUrl;
    }

    public void sendOrderCompleted(OrderCompletedEvent event) {
        // Discord 알림 기능이 비활성화된 경우 전송하지 않음
        if (!enabled) {
            return;
        }

        // 알림은 활성화됐지만 Webhook URL이 없는 경우 경고만 기록
        if (webhookUrl.isBlank()) {
            log.warn(
                    "Discord 알림이 활성화됐지만 Webhook URL이 없습니다."
            );
            return;
        }

        String content = """
                📦 새로운 주문이 접수되었습니다.

                주문 번호: %d
                주문자: %s
                주문 시간: %s
                """.formatted(
                event.orderInfo().orderId(),
                event.orderInfo().email(),
                event.orderedAt().format(ORDERED_AT_FORMATTER)
        );

        DiscordWebhookRequest request =
                new DiscordWebhookRequest(content);

        restClient.post()
                .uri(webhookUrl + "?wait=true")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}