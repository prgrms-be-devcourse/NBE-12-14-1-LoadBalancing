package com.loadbalancing.kiosk.domain.qna.dto;

import jakarta.validation.constraints.NotBlank;

public class QnaRequest {
    public record QnaCreateRequest(
            @NotBlank String email,
            @NotBlank String title,
            @NotBlank String content
    ) {}

    public record QnaAnswerRequest(
            @NotBlank String answer
    ) {}
}