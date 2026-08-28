package com.loadbalancing.kiosk.domain.qna.infra.repository;

import com.loadbalancing.kiosk.domain.qna.infra.entity.Qna;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaRepository extends JpaRepository<Qna, Long> {
   }
