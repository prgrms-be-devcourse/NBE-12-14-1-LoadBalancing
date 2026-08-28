package com.loadbalancing.kiosk.domain.qna.dto;

import com.loadbalancing.kiosk.domain.qna.infra.entity.Qna;
import lombok.Builder;

import java.time.LocalDateTime;

public class QnaResponse {

    @Builder
    public record QnaInfo(
            Long id,
            String email,
            String title,
            String content,
            String answer,
            boolean answered,
            LocalDateTime createdAt
    ) {
        public static QnaInfo from(Qna qna) {
            return QnaInfo.builder()
                    .id(qna.getId())
                    .email(qna.getEmail())
                    .title(qna.getTitle())
                    .content(qna.getContent())
                    .answer(qna.getAnswer())
                    .answered(qna.isAnswered())
                    .createdAt(qna.getCreatedAt())
                    .build();
        }
    }
}