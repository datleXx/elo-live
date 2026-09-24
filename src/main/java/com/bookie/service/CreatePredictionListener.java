package com.bookie.service;

import com.bookie.dto.PredictionMessage;
import com.bookie.engine.EloEngine;
import com.bookie.engine.ExpectedScores;
import com.bookie.engine.MatchResultProb;
import com.bookie.event.PredictionsCreatedEvent;
import com.bookie.event.RatingUpdatedEvent;
import com.bookie.model.Match;
import com.bookie.model.Prediction;
import com.bookie.model.Team;
import com.bookie.repository.MatchRepository;
import com.bookie.repository.PredictionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
public class CreatePredictionListener {
  private final RatingLookupService ratingLookupService;
  private final PredictionRepository predictionRepo;
  private final MatchRepository matchRepo;
  private final EloEngine engine;
  private final ApplicationEventPublisher eventPublisher;

  public CreatePredictionListener(
      RatingLookupService ratingLookupService,
      MatchRepository matchRepository,
      PredictionRepository predictionRepository,
      ApplicationEventPublisher applicationEventPublisher) {
    this.ratingLookupService = ratingLookupService;
    matchRepo = matchRepository;
    predictionRepo = predictionRepository;
    eventPublisher = applicationEventPublisher;
    engine = new EloEngine(20, 100);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onRatingUpdated(RatingUpdatedEvent event) {
    if (event.teamIds().isEmpty()) return;

    List<Prediction> batch = new ArrayList<>();
    List<PredictionMessage> messages = new ArrayList<>();
    Set<List<Long>> dedup = new HashSet<>();
    for (long teamId : event.teamIds()) {
      Optional<Match> nextMatchCheck = matchRepo.findFirstUpcomingFixtureForTeam(teamId);

      if (nextMatchCheck.isEmpty()) continue;
      Match nextMatch = nextMatchCheck.get();

      Team homeTeam = nextMatch.getHomeTeam();
      Team awayTeam = nextMatch.getAwayTeam();

      if (dedup.contains(List.of(homeTeam.getId(), awayTeam.getId()))) continue;

      dedup.add(List.of(homeTeam.getId(), awayTeam.getId()));

      String competition = nextMatch.getCompetition();
      LocalDate fixtureDate = nextMatch.getMatchDate();

      BigDecimal homeTeamBefore =
          ratingLookupService.ratingAt(homeTeam.getId(), competition, fixtureDate);
      BigDecimal awayTeamBefore =
          ratingLookupService.ratingAt(awayTeam.getId(), competition, fixtureDate);
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

      BigDecimal homeWinProb = BigDecimal.valueOf(matchResultProb.homeWinProb());
      BigDecimal drawProb = BigDecimal.valueOf(matchResultProb.drawProb());
      BigDecimal awayWinProb = BigDecimal.valueOf(matchResultProb.awayWinProb());

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

      batch.add(
          new Prediction(
              nextMatch,
              homeTeamBefore,
              awayTeamBefore,
              homeWinProb,
              drawProb,
              awayWinProb,
              marketHomeWinProb,
              marketDrawProb,
              marketAwayWinProb));

      // Built directly from what was just computed - same reason as the
      // rating publish path: nothing here gets re-derived or re-queried
      // later, so there's nothing for a later step to get wrong.
      messages.add(
          new PredictionMessage(
              competition,
              fixtureDate,
              homeTeam.getName(),
              awayTeam.getName(),
              homeWinProb,
              drawProb,
              awayWinProb,
              marketHomeWinProb,
              marketDrawProb,
              marketAwayWinProb));
    }

    if (batch.isEmpty()) return;

    predictionRepo.saveAll(batch);

    eventPublisher.publishEvent(new PredictionsCreatedEvent(messages));
  }
}
