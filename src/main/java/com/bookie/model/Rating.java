package com.bookie.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ratings")
public class Rating {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ratings_seq")
  @SequenceGenerator(name = "ratings_seq", sequenceName = "ratings_id_seq", allocationSize = 50)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "team_id")
  private Team team;

  private BigDecimal rating;

  @ManyToOne
  @JoinColumn(name = "as_of_match_id")
  private Match asOfMatch;
}
