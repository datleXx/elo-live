package com.bookie.service;

import com.bookie.dto.TeamCompetition;
import com.bookie.dto.TeamDivisionSpell;
import com.bookie.model.Match;
import com.bookie.repository.MatchRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TeamDivisionService {
  private final MatchRepository matchRepo;

  public TeamDivisionService(MatchRepository matchRepository) {
    matchRepo = matchRepository;
  }

  /**
   * Splits a team's whole match history into contiguous spells per division — so a team that went
   * E0 -> E1 -> E0 comes back as three spells, not one E0 spell that silently swallows the E1 years
   * in between. GROUP BY alone can't do this: it groups by value, not by "consecutive run of the
   * same value," so this walks the matches in date order instead.
   */
  public List<TeamDivisionSpell> divisionHistory(Long teamId) {
    List<Match> matches =
        matchRepo.findAllForTeamOrderByDate(teamId).stream()
            .filter(m -> !isReplay(m.getCompetition()))
            .toList();
    List<TeamDivisionSpell> spells = new ArrayList<>();

    if (matches.isEmpty()) return spells;

    // The spell currently being built: which competition, and when it started.
    String currentCompetition = matches.get(0).getCompetition();
    LocalDate spellStart = matches.get(0).getMatchDate();
    LocalDate lastDate = matches.get(0).getMatchDate();

    for (int i = 1; i < matches.size(); i++) {
      Match match = matches.get(i);

      if (!match.getCompetition().equals(currentCompetition)) {
        // Competition changed - the spell we were building just ended,
        // on the previous match's date. Close it out and start a new one.
        spells.add(new TeamDivisionSpell(currentCompetition, spellStart, lastDate));
        currentCompetition = match.getCompetition();
        spellStart = match.getMatchDate();
      }

      lastDate = match.getMatchDate();
    }

    // The last spell never hit a "competition changed" boundary, so it never
    // got added inside the loop - close it out here with the final match's date.
    spells.add(new TeamDivisionSpell(currentCompetition, spellStart, lastDate));

    return spells;
  }

  /**
   * One competition per team, for grouping the teams list by country — not their full history, just
   * enough to know which nation's pyramid they belong to. A team can appear as home in some matches
   * and away in others, so both sides get merged; whichever competition is seen last wins, which is
   * fine here since a team practically never changes country.
   */
  public List<TeamCompetition> oneCompetitionPerTeam() {
    Map<Long, String> byTeam = new HashMap<>();

    for (TeamCompetition tc : matchRepo.findHomeTeamCompetitions()) {
      if (!isReplay(tc.competition())) byTeam.put(tc.teamId(), tc.competition());
    }
    for (TeamCompetition tc : matchRepo.findAwayTeamCompetitions()) {
      if (!isReplay(tc.competition())) byTeam.put(tc.teamId(), tc.competition());
    }

    return byTeam.entrySet().stream()
        .map(entry -> new TeamCompetition(entry.getKey(), entry.getValue()))
        .toList();
  }

  // Replay data lives under "{competition}_REPLAY" so it never collides with
  // real ingested data (see reset()) — it's throwaway demo/test data, not a
  // real division a team ever actually played in, so it's excluded here.
  private boolean isReplay(String competition) {
    return competition.endsWith("_REPLAY");
  }
}
