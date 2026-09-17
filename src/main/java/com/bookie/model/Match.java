package com.bookie.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "matches")
public class Match {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "matches_seq")
  @SequenceGenerator(name = "matches_seq", sequenceName = "matches_id_seq", allocationSize = 50)
  private Long id;

  private String competition;
  private LocalDate matchDate;

  @ManyToOne
  @JoinColumn(name = "home_team_id")
  private Team homeTeam;

  @ManyToOne
  @JoinColumn(name = "away_team_id")
  private Team awayTeam;

  private int fullTimeHomeGoals;
  private int fullTimeAwayGoals;

  @Enumerated(EnumType.STRING)
  private MatchResult fullTimeResult;

  private BigDecimal homeOdds;
  private BigDecimal drawOdds;
  private BigDecimal awayOdds;

  protected Match() {}

  public Match(
      String competition,
      LocalDate matchDate,
      Team homeTeam,
      Team awayTeam,
      int fullTimeHomeGoals,
      int fullTimeAwayGoals,
      MatchResult fullTimeResult,
      BigDecimal homeOdds,
      BigDecimal drawOdds,
      BigDecimal awayOdds) {
    this.competition = competition;
    this.matchDate = matchDate;
    this.homeTeam = homeTeam;
    this.awayTeam = awayTeam;
    this.fullTimeHomeGoals = fullTimeHomeGoals;
    this.fullTimeAwayGoals = fullTimeAwayGoals;
    this.fullTimeResult = fullTimeResult;
    this.homeOdds = homeOdds;
    this.drawOdds = drawOdds;
    this.awayOdds = awayOdds;
  }

  public Long getId() {
    return id;
  }

  public String getCompetition() {
    return competition;
  }

  public LocalDate getMatchDate() {
    return matchDate;
  }

  public Team getHomeTeam() {
    return homeTeam;
  }

  public Team getAwayTeam() {
    return awayTeam;
  }

  public int getFullTimeHomeGoals() {
    return fullTimeHomeGoals;
  }

  public int getFullTimeAwayGoals() {
    return fullTimeAwayGoals;
  }

  public MatchResult getFullTimeResult() {
    return fullTimeResult;
  }

  public BigDecimal getHomeOdds() {
    return homeOdds;
  }

  public BigDecimal getDrawOdds() {
    return drawOdds;
  }

  public BigDecimal getAwayOdds() {
    return awayOdds;
  }
}
