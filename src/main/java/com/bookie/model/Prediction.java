package com.bookie.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "predictions")
public class Prediction {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "predictions_seq")
  @SequenceGenerator(
      name = "predictions_seq",
      sequenceName = "predictions_id_seq",
      allocationSize = 50)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "match_id")
  private Match match;

  private BigDecimal homeRatingBefore;
  private BigDecimal awayRatingBefore;
  private BigDecimal homeWinProb;
  private BigDecimal drawProb;
  private BigDecimal awayWinProb;
  private BigDecimal marketHomeWinProb;
  private BigDecimal marketDrawProb;
  private BigDecimal marketAwayWinProb;

  protected Prediction() {}
  ;

  public Prediction(
      Match match,
      BigDecimal homeRatingBefore,
      BigDecimal awayRatingBefore,
      BigDecimal homeWinProb,
      BigDecimal drawProb,
      BigDecimal awayWinProb,
      BigDecimal marketHomeWinProb,
      BigDecimal marketDrawProb,
      BigDecimal marketAwayWinProb) {
    this.match = match;
    this.homeRatingBefore = homeRatingBefore;
    this.awayRatingBefore = awayRatingBefore;
    this.homeWinProb = homeWinProb;
    this.drawProb = drawProb;
    this.awayWinProb = awayWinProb;
    this.marketHomeWinProb = marketHomeWinProb;
    this.marketDrawProb = marketDrawProb;
    this.marketAwayWinProb = marketAwayWinProb;
  }

  public BigDecimal getHomeRatingBefore() {
    return homeRatingBefore;
  }

  public BigDecimal getHomeWinProb() {
    return homeWinProb;
  }

  public BigDecimal getDrawProb() {
    return drawProb;
  }

  public BigDecimal getAwayWinProb() {
    return awayWinProb;
  }

  public BigDecimal getMarketHomeWinProb() {
    return marketHomeWinProb;
  }

  public BigDecimal getMarketDrawProb() {
    return marketDrawProb;
  }

  public BigDecimal getMarketAwayWinProb() {
    return marketAwayWinProb;
  }

  public Match getMatch() {
    return match;
  }

  public Long getId() {
    return id;
  }
}
