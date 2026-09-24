package com.bookie.repository;

import com.bookie.dto.TeamCompetition;
import com.bookie.model.Match;
import com.bookie.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

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

  @Modifying
  @Transactional
  @Query("DELETE FROM Match m WHERE m.competition = :competition")
  void deleteByCompetition(String competition);

  @Query(
      """
              SELECT m FROM Match m
              WHERE m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId
              ORDER BY m.matchDate ASC
              """)
  List<Match> findAllForTeamOrderByDate(Long teamId);

  @Query(
      """
        SELECT DISTINCT new com.bookie.dto.TeamCompetition(t.id, m.competition)
        FROM Match m
        JOIN m.homeTeam t
        """)
  List<TeamCompetition> findHomeTeamCompetitions();

  @Query(
      """
        SELECT DISTINCT new com.bookie.dto.TeamCompetition(t.id, m.competition)
        FROM Match m
        JOIN m.awayTeam t
        """)
  List<TeamCompetition> findAwayTeamCompetitions();
}
