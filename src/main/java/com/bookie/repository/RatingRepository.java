package com.bookie.repository;

import com.bookie.dto.RatingUpdateMessage;
import com.bookie.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
  Optional<Rating> findByTeamIdAndAsOfMatchId(Long teamId, Long matchId);

  Optional<Rating> findFirstByTeamIdOrderByAsOfMatch_MatchDateDesc(Long teamId);

  // "Before this match" has two separate requirements: it must not look past
  // this match's own date (lookahead bias - bulk-ingesting divisions one at a
  // time, not interleaved by calendar date, means a team's future rating in
  // one division can otherwise leak backward into an old match in another),
  // and it must not cross the real/replay boundary (a throwaway replay run
  // must never read or feed back into real data). Real data stays continuous
  // across every real division on purpose - that's what lets a team's rating
  // carry through an actual promotion or relegation.
  @Query(
      """
        SELECT r FROM Rating r
        WHERE r.team.id = :teamId
            AND r.asOfMatch.matchDate <= :date
            AND r.asOfMatch.competition NOT LIKE '%\\_REPLAY' ESCAPE '\\'
        ORDER BY r.asOfMatch.matchDate DESC
        LIMIT 1
        """)
  Optional<Rating> findLatestRealRatingForTeamBeforeDate(Long teamId, LocalDate date);

  @Query(
      """
        SELECT r FROM Rating r
        WHERE r.team.id = :teamId
            AND r.asOfMatch.competition = :competition
            AND r.asOfMatch.matchDate <= :date
        ORDER BY r.asOfMatch.matchDate DESC
        LIMIT 1
        """)
  Optional<Rating> findLatestForTeamInCompetitionBeforeDate(
      Long teamId, String competition, LocalDate date);

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
        WHERE t.id = :teamId
        ORDER BY m.matchDate ASC
        """)
  List<RatingUpdateMessage> findRatingHistoryForTeam(Long teamId);

  @Query(
      """
        SELECT new com.bookie.dto.RatingUpdateMessage(m.competition, t.id, t.name, r.rating, m.matchDate)
        FROM Rating r
        JOIN r.team t
        JOIN r.asOfMatch m
        WHERE m.competition = :competition
            AND m.matchDate = (
                SELECT MAX(m2.matchDate)
                FROM Rating r2
                JOIN r2.asOfMatch m2
                WHERE r2.team = r.team AND m2.competition = :competition
            )
        """)
  List<RatingUpdateMessage> findCurrentRatingsForCompetition(String competition);

  @Transactional
  void deleteByTeamIdIn(List<Long> teamIds);

  @Modifying
  @Transactional
  @Query("DELETE FROM Rating r WHERE r.asOfMatch.competition = :competition")
  void deleteByAsOfMatch_Competition(String competition);
}
