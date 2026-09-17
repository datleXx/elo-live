package com.bookie.repository;

import com.bookie.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
  Optional<Rating> findByTeamIdAndAsOfMatchId(Long teamId, Long matchId);
}
