package com.bookie.repository;

import com.bookie.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
  List<Match> findByCompetition(String competition);
}
