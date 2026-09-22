package com.bookie.repository;

import com.bookie.model.Match;
import com.bookie.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
  List<Match> findByCompetition(String competition);

  List<Match> findByCompetitionOrderByMatchDateAsc(String competition);

  @Query(
      """
        SELECT m FROM Match m
        JOIN FETCH m.homeTeam
        JOIN FETCH m.awayTeam
        WHERE m.id = :id
        """)
  Optional<Match> findByIdWithTeams(Long id);

  @Query(
      """
          SELECT m FROM Match m
          JOIN FETCH m.homeTeam
          JOIN FETCH m.awayTeam
          WHERE m.id IN :ids
          """)
  List<Match> findByIdsWithTeams(List<Long> ids);

  @Query(
      """
              SELECT m FROM Match m
              WHERE (m.homeTeam.id IN :teamIds OR m.awayTeam.id IN :teamIds)
                  AND m.fullTimeHomeGoals IS NULL
                      AND m.fullTimeAwayGoals IS NULL
                          AND m.fullTimeResult IS NULL
              ORDER BY m.matchDate ASC, m.id ASC
              """)
  List<Match> findUpcomingFixuresByTeam(List<Long> teamIds);

  @Query(
      """
                  SELECT m FROM Match m
                                    JOIN FETCH m.homeTeam
                                                      JOIN FETCH m.awayTeam
                  WHERE (m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId)
                      AND m.fullTimeHomeGoals IS NULL
                          AND m.fullTimeAwayGoals IS NULL
                              AND m.fullTimeResult IS NULL
                  ORDER BY m.matchDate ASC
                                    LIMIT 1
                  """)
  Optional<Match> findFirstUpcomingFixtureForTeam(Long teamId);

  List<Match> findByCompetitionAndMatchDate(String competition, LocalDate date);
}
