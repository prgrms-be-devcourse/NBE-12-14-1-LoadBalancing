package com.loadbalancing.kiosk.domain.qna.controller;

import com.loadbalancing.kiosk.domain.qna.dto.QnaRequest;
import com.loadbalancing.kiosk.domain.qna.dto.QnaRequest.QnaCreateRequest;
import com.loadbalancing.kiosk.domain.qna.dto.QnaResponse.QnaDetailResponse;
import com.loadbalancing.kiosk.domain.qna.dto.QnaResponse.QnaListResponse;
import com.loadbalancing.kiosk.domain.qna.service.QnaService;
import com.loadbalancing.kiosk.global.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;

    @PostMapping
    public ResponseEntity<ApiResponse<QnaDetailResponse>> create(@Valid @RequestBody QnaCreateRequest request) {
        QnaDetailResponse response = qnaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<QnaListResponse>>> list(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<QnaListResponse> response = qnaService.getList(pageable);
        return ResponseEntity.ok(ApiResponse.success(200, response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QnaDetailResponse>> detail(@PathVariable Long id) {
        QnaDetailResponse response = qnaService.getDetail(id);
        return ResponseEntity.ok(ApiResponse.success(200, response));
    }

    @PatchMapping("/{id}/answer")
    public ResponseEntity<ApiResponse<QnaDetailResponse>> answer(
            @PathVariable Long id,
            @Valid @RequestBody QnaRequest.QnaAnswerRequest request
    ) {
        QnaDetailResponse response = qnaService.answer(id, request.answer());
        return ResponseEntity.ok(ApiResponse.success(200, response));
    }
}