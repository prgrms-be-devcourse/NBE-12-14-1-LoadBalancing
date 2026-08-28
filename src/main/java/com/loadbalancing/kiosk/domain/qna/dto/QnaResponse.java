package com.loadbalancing.kiosk.domain.qna.dto;

import com.loadbalancing.kiosk.domain.qna.infra.entity.Qna;

import java.time.LocalDateTime;
public class QnaResponse {
    public record QnaListResponse(
            Long id,
            String title,
            String email,
            boolean answered,
            LocalDateTime createdAt
    ) {
        public static QnaListResponse from(Qna qna) {
            return new QnaListResponse(qna.getId(), qna.getTitle(), qna.getEmail(), qna.isAnswered(), qna.getCreatedAt());
        }
    }

    public record QnaDetailResponse(
            Long id,
            String email,
            String title,
            String content,
            String answer,
            boolean answered,
            LocalDateTime createdAt
    ) {
        public static QnaDetailResponse from(Qna qna) {
            return new QnaDetailResponse(qna.getId(), qna.getEmail(), qna.getTitle(), qna.getContent(), qna.getAnswer(), qna.isAnswered(), qna.getCreatedAt());
        }
    }
}
