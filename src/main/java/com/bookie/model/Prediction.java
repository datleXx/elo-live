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
}
