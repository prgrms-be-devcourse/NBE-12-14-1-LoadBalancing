package com.loadbalancing.kiosk.domain.qna.service;

import com.loadbalancing.kiosk.domain.qna.dto.QnaRequest.QnaCreateRequest;
import com.loadbalancing.kiosk.domain.qna.dto.QnaResponse.QnaDetailResponse;
import com.loadbalancing.kiosk.domain.qna.dto.QnaResponse.QnaListResponse;
import com.loadbalancing.kiosk.domain.qna.infra.entity.Qna;
import com.loadbalancing.kiosk.domain.qna.infra.repository.QnaRepository;
import com.loadbalancing.kiosk.global.exception.custom.QnaNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class QnaService {

    private final QnaRepository qnaRepository;

    @Transactional
    public QnaDetailResponse create(QnaCreateRequest request) {
        Qna qna = Qna.builder()
                            .email(request.email())
                            .title(request.title())
                            .content(request.content())
                            .answered(false)
                            .build();
        return QnaDetailResponse.from(qnaRepository.save(qna));
    }

    @Transactional(readOnly = true)
    public QnaDetailResponse getDetail(Long id) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new QnaNotFoundException(id));
        return QnaDetailResponse.from(qna);
    }

    @Transactional(readOnly = true)
    public Page<QnaListResponse> getList(Pageable pageable) {
        return qnaRepository.findAll(pageable).map(QnaListResponse::from);
    }

    @Transactional
    public QnaDetailResponse answer(Long id, String answer) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new QnaNotFoundException(id));
        qna.answer(answer);
        return QnaDetailResponse.from(qna);
    }
}