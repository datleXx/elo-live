package com.bookie.repository;

import com.bookie.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
  Optional<Team> findByName(String name);

  List<Team> findByNameIn(List<String> names);

  List<Team> findByOrderByNameAsc();
}
