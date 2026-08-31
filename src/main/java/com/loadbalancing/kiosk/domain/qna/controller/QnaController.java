package com.loadbalancing.kiosk.domain.qna.controller;

import com.loadbalancing.kiosk.domain.qna.dto.QnaRequest;
import com.loadbalancing.kiosk.domain.qna.dto.QnaRequest.QnaCreateRequest;
import com.loadbalancing.kiosk.domain.qna.dto.QnaResponse.QnaInfo;
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
@RequestMapping("/api/v1/auth/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;

    @PostMapping
    public ResponseEntity<ApiResponse<QnaInfo>> create(@Valid @RequestBody QnaCreateRequest request) {
        QnaInfo response = qnaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, response));
    }

    // email 없이 호출하면 전체 목록(관리자 화면용), email과 함께 호출하면 그 이메일 것만(고객이 자기 문의 확인용)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<QnaInfo>>> list(
            @RequestParam(required = false) String email,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<QnaInfo> response = qnaService.getList(email, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QnaInfo>> detail(@PathVariable Long id) {
        QnaInfo response = qnaService.getDetail(id);
        return ResponseEntity.ok(ApiResponse.success(200, response));
    }

    @PatchMapping("/{id}/answer")
    public ResponseEntity<ApiResponse<QnaInfo>> answer(
            @PathVariable Long id,
            @Valid @RequestBody QnaRequest.QnaAnswerRequest request
    ) {
        QnaInfo response = qnaService.answer(id, request.answer());
        return ResponseEntity.ok(ApiResponse.success(200, response));
    }
}