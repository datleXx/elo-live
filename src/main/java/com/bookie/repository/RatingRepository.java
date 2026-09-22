package com.bookie.repository;

import com.bookie.dto.RatingUpdateMessage;
import com.bookie.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
  Optional<Rating> findByTeamIdAndAsOfMatchId(Long teamId, Long matchId);

  Optional<Rating> findFirstByTeamIdOrderByAsOfMatch_MatchDateDesc(Long teamId);

  @Query(
      """
        SELECT r.rating FROM Rating r
        WHERE r.team.id = :teamId
            AND r.asOfMatch.matchDate <= :date
        ORDER BY r.asOfMatch.matchDate DESC
        LIMIT 1
        """)
  Optional<BigDecimal> findFirstByTeamIdBeforeDate(Long teamId, LocalDate date);

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

  @Transactional
  void deleteByAsOfMatch_Competition(String competition);
}
