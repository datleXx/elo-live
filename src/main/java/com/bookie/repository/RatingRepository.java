package com.bookie.repository;

import com.bookie.dto.RatingUpdateMessage;
import com.bookie.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
  Optional<Rating> findByTeamIdAndAsOfMatchId(Long teamId, Long matchId);

  Optional<Rating> findFirstByTeamIdOrderByAsOfMatch_MatchDateDesc(Long teamId);

  @Query(
      """
        SELECT new com.bookie.dto.RatingUpdateMessage(m.competition, t.id, t.name, r.rating, m.matchDate)
        FROM Rating r
        JOIN r.team t
        JOIN r.asOfMatch m
        WHERE t.id IN :teamIds
            AND m.matchDate = (
                SELECT MAX(m2.matchDate)
                FROM Rating r2
                JOIN r2.asOfMatch m2
                WHERE r2.team = r.team
            )
        """)
  List<RatingUpdateMessage> findRatingUpdateMessages(List<Long> teamIds);

  @Transactional
  void deleteByTeamIdIn(List<Long> teamIds);
}
