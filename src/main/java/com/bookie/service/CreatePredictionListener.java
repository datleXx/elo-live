package com.bookie.service;

import com.bookie.engine.EloEngine;
import com.bookie.engine.ExpectedScores;
import com.bookie.engine.MatchResultProb;
import com.bookie.event.RatingUpdatedEvent;
import com.bookie.model.Match;
import com.bookie.model.Prediction;
import com.bookie.model.Rating;
import com.bookie.model.Team;
import com.bookie.repository.MatchRepository;
import com.bookie.repository.PredictionRepository;
import com.bookie.repository.RatingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.*;

@Component
public class CreatePredictionListener {
  private final PredictionRepository predictionRepo;
  private final RatingRepository ratingRepo;
  private final MatchRepository matchRepo;
  private final EloEngine engine;

  public CreatePredictionListener(
      RatingRepository ratingRepository,
      MatchRepository matchRepository,
      PredictionRepository predictionRepository) {
    ratingRepo = ratingRepository;
    matchRepo = matchRepository;
    predictionRepo = predictionRepository;
    engine = new EloEngine(20, 100);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onRatingUpdated(RatingUpdatedEvent event) {
    List<Prediction> batch = new ArrayList<>();
    Set<List<Long>> dedup = new HashSet<>();
    for (long teamId : event.teamIds()) {
      Optional<Match> nextMatchCheck = matchRepo.findFirstUpcomingFixtureForTeam(teamId);

      if (nextMatchCheck.isEmpty()) continue;
      Match nextMatch = nextMatchCheck.get();

      Team homeTeam = nextMatch.getHomeTeam();
      Team awayTeam = nextMatch.getAwayTeam();

      if (dedup.contains(List.of(homeTeam.getId(), awayTeam.getId()))) continue;

      dedup.add(List.of(homeTeam.getId(), awayTeam.getId()));

      BigDecimal homeTeamBefore =
          ratingRepo
              .findFirstByTeamIdOrderByAsOfMatch_MatchDateDesc(homeTeam.getId())
              .map(Rating::getRating)
              .orElse(BigDecimal.valueOf(1500));
      BigDecimal awayTeamBefore =
          ratingRepo
              .findFirstByTeamIdOrderByAsOfMatch_MatchDateDesc(awayTeam.getId())
              .map(Rating::getRating)
              .orElse(BigDecimal.valueOf(1500));
      ExpectedScores nextMatchExpectedScores =
          engine.calExpectedScores(homeTeamBefore.doubleValue(), awayTeamBefore.doubleValue());
      MatchResultProb matchResultProb =
          engine.calMatchResultProb(
              homeTeamBefore.doubleValue(),
              awayTeamBefore.doubleValue(),
              nextMatchExpectedScores.home());
      MatchResultProb marketResultProb =
          engine.calMarketMatchResultProb(
              nextMatch.getHomeOdds(), nextMatch.getDrawOdds(), nextMatch.getAwayOdds());

      BigDecimal marketHomeWinProb =
          marketResultProb.homeWinProb() != null
              ? BigDecimal.valueOf(marketResultProb.homeWinProb())
              : null;
      BigDecimal marketDrawProb =
          marketResultProb.drawProb() != null
              ? BigDecimal.valueOf(marketResultProb.drawProb())
              : null;
      BigDecimal marketAwayWinProb =
          marketResultProb.awayWinProb() != null
              ? BigDecimal.valueOf(marketResultProb.awayWinProb())
              : null;

      Prediction nextMatchPrediction =
          new Prediction(
              nextMatch,
              homeTeamBefore,
              awayTeamBefore,
              BigDecimal.valueOf(matchResultProb.homeWinProb()),
              BigDecimal.valueOf(matchResultProb.drawProb()),
              BigDecimal.valueOf(matchResultProb.awayWinProb()),
              marketHomeWinProb,
              marketDrawProb,
              marketAwayWinProb);
      batch.add(nextMatchPrediction);
    }

    predictionRepo.saveAll(batch);
  }
}
