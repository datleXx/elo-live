package com.bookie.model;

import jakarta.persistence.*;

@Entity
@Table(name = "teams")
public class Team {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "teams_seq")
  @SequenceGenerator(name = "teams_seq", sequenceName = "teams_id_seq", allocationSize = 50)
  private Long id;

  private String name;

  protected Team() {}

  public Team(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
