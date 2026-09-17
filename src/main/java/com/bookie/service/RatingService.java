package com.bookie.service;

import com.bookie.engine.Elo;
import com.bookie.engine.EloEngine;
import com.bookie.engine.ExpectedScores;
import com.bookie.engine.MatchResultProb;
import com.bookie.model.Match;
import com.bookie.model.Prediction;
import com.bookie.model.Rating;
import com.bookie.repository.MatchRepository;
import com.bookie.repository.PredictionRepository;
import com.bookie.repository.RatingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class RatingService {
  private final MatchRepository matchRepo;
  private final RatingRepository ratingRepo;
  private final PredictionRepository predictionRepo;
  private final EloEngine eloEngine;

  public RatingService(
      MatchRepository matchRepo, RatingRepository ratingRepo, PredictionRepository predictionRepo) {
    this.matchRepo = matchRepo;
    this.ratingRepo = ratingRepo;
    this.predictionRepo = predictionRepo;
    eloEngine = new EloEngine(20, 100);
  }

  @Transactional
  public void backTestByCompetition(String competition) {
    Map<Long, Double> teamRatingMap = new HashMap<>();
    List<Match> allCompMatches = matchRepo.findByCompetitionOrderByMatchDateAsc(competition);
    List<Prediction> batchPredictions = new ArrayList<>();
    List<Rating> batchRatings = new ArrayList<>();

    for (Match processingMatch : allCompMatches) {
      Double homeRatingBefore =
          teamRatingMap.getOrDefault(processingMatch.getHomeTeam().getId(), Double.valueOf(1500));
      Double awayRatingBefore =
          teamRatingMap.getOrDefault(processingMatch.getAwayTeam().getId(), Double.valueOf(1500));

      ExpectedScores expectedScores =
          eloEngine.calExpectedScores(homeRatingBefore, awayRatingBefore);

      MatchResultProb matchResultProb =
          eloEngine.calMatchResultProb(homeRatingBefore, awayRatingBefore, expectedScores.home());

      Elo updatedRating =
          eloEngine.calElos(
              processingMatch.getFullTimeHomeGoals(),
              processingMatch.getFullTimeAwayGoals(),
              homeRatingBefore,
              awayRatingBefore);

      Rating newHomeRatingToSave =
          new Rating(
              processingMatch.getHomeTeam(),
              BigDecimal.valueOf(updatedRating.home()),
              processingMatch);

      Rating newAwayRatingToSave =
          new Rating(
              processingMatch.getAwayTeam(),
              BigDecimal.valueOf(updatedRating.away()),
              processingMatch);

      Prediction predictionToSave =
          new Prediction(
              processingMatch,
              BigDecimal.valueOf(homeRatingBefore),
              BigDecimal.valueOf(awayRatingBefore),
              BigDecimal.valueOf(matchResultProb.homeWinProb()),
              BigDecimal.valueOf(matchResultProb.drawProb()),
              BigDecimal.valueOf(matchResultProb.awayWinProb()),
              processingMatch.getHomeOdds(),
              processingMatch.getDrawOdds(),
              processingMatch.getAwayOdds());

      batchRatings.add(newHomeRatingToSave);
      batchRatings.add(newAwayRatingToSave);
      batchPredictions.add(predictionToSave);
    }

    ratingRepo.saveAll(batchRatings);
    predictionRepo.saveAll(batchPredictions);
  }
}
