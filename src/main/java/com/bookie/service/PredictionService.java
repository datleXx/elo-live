package com.bookie.service;

import com.bookie.model.Match;
import com.bookie.model.MatchResult;
import com.bookie.model.Prediction;
import com.bookie.repository.PredictionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PredictionService {
  private final PredictionRepository predictionRepo;

  public PredictionService(PredictionRepository predictionRepo) {
    this.predictionRepo = predictionRepo;
  }

  private MatchResult pick(Double home, Double draw, Double away) {
    if (home > draw && home > away) return MatchResult.H;
    else if (away > draw && away > home) return MatchResult.A;

    return MatchResult.D;
  }

  public BacktestReport evaluateAccuracy(String competition) {
    List<Prediction> predictionsByComp = predictionRepo.findForCompetition(competition);

    List<Prediction> withOdds =
        predictionsByComp.stream()
            .filter(p -> p.getMarketHomeWinProb() != null)
            .filter(p -> p.getMarketDrawProb() != null)
            .filter(p -> p.getMarketAwayWinProb() != null)
            .toList();

    int modelCorrectCount = 0, marketCorrectCount = 0, totalFixtures = 0;
    List<Match> disagreementList = new ArrayList<>();

    for (Prediction prediction : withOdds) {
      Match match = prediction.getMatch();

      MatchResult modelPick =
          pick(
              prediction.getHomeWinProb().doubleValue(),
              prediction.getDrawProb().doubleValue(),
              prediction.getAwayWinProb().doubleValue());

      MatchResult marketPick =
          pick(
              prediction.getMarketHomeWinProb().doubleValue(),
              prediction.getMarketDrawProb().doubleValue(),
              prediction.getMarketAwayWinProb().doubleValue());

      if (modelPick == match.getFullTimeResult()) modelCorrectCount++;
      if (marketPick == match.getFullTimeResult()) marketCorrectCount++;
      if (marketPick != null && modelPick != marketPick) disagreementList.add(match);
      totalFixtures++;
    }

    return new BacktestReport(
        modelCorrectCount, marketCorrectCount, totalFixtures, disagreementList);
  }
}
