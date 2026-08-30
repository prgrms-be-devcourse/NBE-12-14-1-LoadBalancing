package com.loadbalancing.kiosk.domain.qna.service;

import com.loadbalancing.kiosk.domain.qna.dto.QnaRequest.QnaCreateRequest;
import com.loadbalancing.kiosk.domain.qna.dto.QnaResponse.QnaInfo;
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
    public QnaInfo create(QnaCreateRequest request) {
        Qna qna = Qna.builder()
                .email(request.email())
                .title(request.title())
                .content(request.content())
                .answered(false)
                .build();
        return QnaInfo.from(qnaRepository.save(qna));
    }

    @Transactional(readOnly = true)
    public QnaInfo getDetail(Long id) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new QnaNotFoundException(id));
        return QnaInfo.from(qna);
    }

    // email이 없으면 전체 목록(관리자용), 있으면 그 이메일 것만(고객이 자기 문의/답변 확인용).
    // ProductService 가격검색과 같은 패턴 - 프론트에서 관리자 화면은 email 없이, 고객 화면은 email과 함께 호출함
    @Transactional(readOnly = true)
    public Page<QnaInfo> getList(String email, Pageable pageable) {
        Page<Qna> qnas = (email == null || email.isBlank())
                ? qnaRepository.findAll(pageable)
                : qnaRepository.findAllByEmail(email, pageable);
        return qnas.map(QnaInfo::from);
    }

    @Transactional
    public QnaInfo answer(Long id, String answer) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new QnaNotFoundException(id));
        qna.answer(answer);
        return QnaInfo.from(qna);
    }
}