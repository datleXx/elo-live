package com.bookie.controller;

import com.bookie.dto.RatingUpdateMessage;
import com.bookie.dto.TeamCompetition;
import com.bookie.dto.TeamDivisionSpell;
import com.bookie.model.Team;
import com.bookie.repository.RatingRepository;
import com.bookie.repository.TeamRepository;
import com.bookie.service.TeamDivisionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TeamController {
  private final TeamRepository teamRepo;
  private final RatingRepository ratingRepo;
  private final TeamDivisionService teamDivisionService;

  public TeamController(
      TeamRepository teamRepository,
      RatingRepository ratingRepository,
      TeamDivisionService teamDivisionService) {
    teamRepo = teamRepository;
    ratingRepo = ratingRepository;
    this.teamDivisionService = teamDivisionService;
  }

  @GetMapping("/api/teams")
  public List<Team> teams() {
    return teamRepo.findByOrderByNameAsc();
  }

  @GetMapping("/api/teams/{id}/ratings")
  public List<RatingUpdateMessage> ratingHistory(@PathVariable Long id) {
    return ratingRepo.findRatingHistoryForTeam(id);
  }

  @GetMapping("/api/teams/{id}/divisions")
  public List<TeamDivisionSpell> divisionHistory(@PathVariable Long id) {
    return teamDivisionService.divisionHistory(id);
  }

  @GetMapping("/api/teams/competitions")
  public List<TeamCompetition> teamCompetitions() {
    return teamDivisionService.oneCompetitionPerTeam();
  }
}
