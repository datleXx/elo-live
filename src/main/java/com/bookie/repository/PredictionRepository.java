package com.bookie.repository;

import com.bookie.model.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {
  Optional<Prediction> findByMatchId(Long id);

  @Query(
      """
              SELECT p FROM Prediction p
              JOIN FETCH p.match m
              WHERE m.competition = :competition
              ORDER BY m.matchDate ASC, m.id ASC
              """)
  List<Prediction> findForCompetition(String competition);

  @Modifying
  @Transactional
  @Query("DELETE FROM Prediction p WHERE p.match.competition = :competition")
  void deleteByMatch_Competition(String competition);
}
