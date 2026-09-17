package com.bookie.repository;

import com.bookie.model.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {
  Optional<Prediction> findByMatchId(Long id);
}
