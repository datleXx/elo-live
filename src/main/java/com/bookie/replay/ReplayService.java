package com.bookie.replay;

import com.bookie.event.MatchResultIngestedEvent;
import com.bookie.model.Match;
import com.bookie.model.Team;
import com.bookie.repository.MatchRepository;
import com.bookie.repository.PredictionRepository;
import com.bookie.repository.RatingRepository;
import com.bookie.repository.TeamRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class ReplayService {
 private final MatchRepository matchRepo;
  private final RatingRepository ratingRepo;
  private final PredictionRepository predictionRepo;
  private final TeamRepository teamRepo;
  private final ApplicationEventPublisher eventPublisher;

  public ReplayService(
      MatchRepository matchRepository,
      RatingRepository ratingRepository,
      PredictionRepository predictionRepository,
      TeamRepository teamRepository,
      ApplicationEventPublisher applicationEventPublisher) {
    matchRepo = matchRepository;
    ratingRepo = ratingRepository;
    predictionRepo = predictionRepository;
    teamRepo = teamRepository;
    eventPublisher = applicationEventPublisher;
  }

  @Transactional
  public void reset(String competition, List<List<ReplayRow>> allRows) {
    ratingRepo.deleteByAsOfMatch_Competition(competition + "_REPLAY");
    predictionRepo.deleteByMatch_Competition(competition + "_REPLAY");
    matchRepo.deleteByCompetition(competition + "_REPLAY");

    Map<String, Team> teamMap = new HashMap<>();
    List<Team> allTeams = teamRepo.findAll();
    allTeams.forEach(team -> teamMap.put(team.getName(), team));

    List<ReplayRow> allRowsFlatten = allRows.stream().flatMap(listRow -> listRow.stream()).toList();
    List<Match> batch = new ArrayList<>();

    allRowsFlatten.forEach(
        row -> {
          Team homeTeam = teamMap.get(row.homeTeam());
          Team awayTeam = teamMap.get(row.awayTeam());

          if (homeTeam == null) homeTeam = teamRepo.save(new Team(row.homeTeam()));
          if (awayTeam == null) awayTeam = teamRepo.save(new Team(row.awayTeam()));

          batch.add(
              new Match(
                  competition + "_REPLAY",
                  row.date(),
                  homeTeam,
                  awayTeam,
                  null,
                  null,
                  row.homeOdds(),
                  row.drawOdds(),
                  row.awayOdds()));
        });

    matchRepo.saveAll(batch);
  }

  @Transactional
  public void tick(String competition, List<ReplayRow> today) {
    if (today.isEmpty()) return;

    LocalDate matchDate = today.getFirst().date();
    Map<String, Match> matchesMap = new HashMap<>();
    List<Match> todayMatches =
        matchRepo.findByCompetitionAndMatchDate(competition + "_REPLAY", matchDate);

    if (todayMatches.isEmpty())
      throw new IllegalStateException("Missing matches for date " + matchDate);

    todayMatches.forEach(
        match -> {
          String key =
              match.getHomeTeam().getName().trim() + "/" + match.getAwayTeam().getName().trim();
          matchesMap.put(key, match);
        });

    today.forEach(
        replay -> {
          String key = replay.homeTeam() + "/" + replay.awayTeam();
          Match match = matchesMap.get(key);

          if (match == null)
            throw new IllegalStateException(
                "Missing match between "
                    + replay.homeTeam()
                    + "/"
                    + replay.awayTeam()
                    + " for date "
                    + matchDate);

          match.recordResults(replay.homeGoals(), replay.awayGoals());
        });

    eventPublisher.publishEvent(
        new MatchResultIngestedEvent(todayMatches.stream().map(match -> match.getId()).toList()));
  }
}
