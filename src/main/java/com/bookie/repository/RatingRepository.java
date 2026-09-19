package com.bookie.repository;

import com.bookie.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
  Optional<Rating> findByTeamIdAndAsOfMatchId(Long teamId, Long matchId);

  Optional<Rating> findFirstByTeamIdOrderByAsOfMatch_MatchDateDesc(Long teamId);

  @Transactional
  void deleteByTeamIdIn(List<Long> teamIds);
}
