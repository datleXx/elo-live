package com.bookie.service;

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
import java.util.ArrayList;
import java.util.List;

@Component
public class UpdateRatingListener {
  private final MatchRepository matchRepo;
  private final RatingRepository ratingRepo;
  private final ApplicationEventPublisher eventPublisher;

  private final EloEngine engine;

  public UpdateRatingListener(
      MatchRepository matchRepository,
      RatingRepository ratingRepository,
      ApplicationEventPublisher applicationEventPublisher) {
    matchRepo = matchRepository;
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

    for (Match updatedMatch : finishedMatches) {
      Team homeTeam = updatedMatch.getHomeTeam();
      Team awayTeam = updatedMatch.getAwayTeam();

      BigDecimal homeRatingBefore =
          ratingRepo
              .findFirstByTeamIdOrderByAsOfMatch_MatchDateDesc(homeTeam.getId())
              .map(Rating::getRating)
              .orElse(BigDecimal.valueOf(1500));

      BigDecimal awayRatingBefore =
          ratingRepo
              .findFirstByTeamIdOrderByAsOfMatch_MatchDateDesc(awayTeam.getId())
              .map(Rating::getRating)
              .orElse(BigDecimal.valueOf(1500));

      Elo updatedRating =
          engine.calElos(
              updatedMatch.getFullTimeHomeGoals(),
              updatedMatch.getFullTimeAwayGoals(),
              homeRatingBefore.doubleValue(),
              awayRatingBefore.doubleValue());

      batch.add(new Rating(homeTeam, BigDecimal.valueOf(updatedRating.home()), updatedMatch));
      batch.add(new Rating(awayTeam, BigDecimal.valueOf(updatedRating.away()), updatedMatch));
    }

    ratingRepo.saveAll(batch);

    List<Long> teamIds = batch.stream().map(rating -> rating.getTeam().getId()).distinct().toList();
    eventPublisher.publishEvent(new RatingUpdatedEvent(teamIds));
  }
}
