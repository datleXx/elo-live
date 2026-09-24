package com.bookie.service;

import com.bookie.dto.MatchResultMessage;
import com.bookie.engine.Elo;
import com.bookie.engine.EloEngine;
import com.bookie.event.MatchResultIngestedEvent;
import com.bookie.event.RatingUpdatedEvent;
import com.bookie.model.Match;
import com.bookie.model.Rating;
import com.bookie.model.Team;
import com.bookie.repository.MatchRepository;
import com.bookie.repository.RatingRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class UpdateRatingListener {
  private final MatchRepository matchRepo;
  private final RatingLookupService lookupService;
  private final RatingRepository ratingRepo;
  private final ApplicationEventPublisher eventPublisher;

  private final EloEngine engine;

  public UpdateRatingListener(
      MatchRepository matchRepository,
      RatingLookupService ratingLookupService,
      RatingRepository ratingRepository,
      ApplicationEventPublisher applicationEventPublisher) {
    matchRepo = matchRepository;
    lookupService = ratingLookupService;
    ratingRepo = ratingRepository;
    eventPublisher = applicationEventPublisher;

    engine = new EloEngine(20, 100);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onMatchIngested(MatchResultIngestedEvent event) {
    List<Match> insertedMatches = matchRepo.findByIdsWithTeams(event.matchIds());
    List<Match> finishedMatches =
        insertedMatches.stream()
            .filter(
                match ->
                    match.getFullTimeResult() != null
                        && match.getFullTimeAwayGoals() != null
                        && match.getFullTimeHomeGoals() != null)
            .toList();
    List<Rating> batch = new ArrayList<>();
    List<MatchResultMessage> results = new ArrayList<>();

    for (Match updatedMatch : finishedMatches) {
      Team homeTeam = updatedMatch.getHomeTeam();
      Team awayTeam = updatedMatch.getAwayTeam();
      String competition = updatedMatch.getCompetition();

      boolean isReplay = competition.endsWith("_REPLAY");
      LocalDate matchDate = updatedMatch.getMatchDate();
      String competitionToSave =
          !isReplay
              ? competition
              : competition.substring(0, competition.length() - "_REPLAY".length());

      BigDecimal homeRatingBefore =
          lookupService.ratingAt(homeTeam.getId(), competitionToSave, matchDate);

      BigDecimal awayRatingBefore =
          lookupService.ratingAt(awayTeam.getId(), competitionToSave, matchDate);

      Elo updatedRating =
          engine.calElos(
              updatedMatch.getFullTimeHomeGoals(),
              updatedMatch.getFullTimeAwayGoals(),
              homeRatingBefore.doubleValue(),
              awayRatingBefore.doubleValue());

      BigDecimal homeRatingAfter = BigDecimal.valueOf(updatedRating.home());
      BigDecimal awayRatingAfter = BigDecimal.valueOf(updatedRating.away());

      batch.add(new Rating(homeTeam, homeRatingAfter, updatedMatch));
      batch.add(new Rating(awayTeam, awayRatingAfter, updatedMatch));

      // Built directly from what was just computed - nothing gets re-derived
      // or re-queried later, so there's nothing for a later step to get wrong.
      results.add(
          new MatchResultMessage(
              competition,
              updatedMatch.getMatchDate(),
              homeTeam.getName(),
              awayTeam.getName(),
              updatedMatch.getFullTimeHomeGoals(),
              updatedMatch.getFullTimeAwayGoals(),
              homeRatingBefore,
              awayRatingBefore,
              homeRatingAfter,
              awayRatingAfter));
    }

    ratingRepo.saveAll(batch);

    List<Long> teamIds = batch.stream().map(rating -> rating.getTeam().getId()).distinct().toList();
    eventPublisher.publishEvent(new RatingUpdatedEvent(teamIds, results));
  }
}
