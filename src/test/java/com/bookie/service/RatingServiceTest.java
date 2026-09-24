package com.bookie.service;

import com.bookie.model.Match;
import com.bookie.model.MatchResult;
import com.bookie.model.Prediction;
import com.bookie.model.Team;
import com.bookie.repository.MatchRepository;
import com.bookie.repository.PredictionRepository;
import com.bookie.repository.TeamRepository;
import com.bookie.util.Constant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
public class RatingServiceTest {
  @Autowired TeamRepository teamRepo;
  @Autowired MatchRepository matchRepo;
  @Autowired PredictionRepository predictionRepo;
  @Autowired RatingService ratingService;

  @Test
  void earlyPredictionIgnoresFutureResults() {
    Team teamA = teamRepo.save(new Team("Manchester United"));
    Team teamB = teamRepo.save(new Team("Manchester City"));
    Team teamC = teamRepo.save(new Team("Liverpool"));

    Match matchA =
        matchRepo.save(
            new Match("E0", LocalDate.of(2026, 1, 1), teamA, teamB, 2, 1, null, null, null));

    Match matchB =
        matchRepo.save(
            new Match("E0", LocalDate.of(2026, 6, 1), teamA, teamC, 6, 1, null, null, null));

    ratingService.backTestByCompetition("E0");
    Prediction januaryPrediction = predictionRepo.findByMatchId(matchA.getId()).orElseThrow();

    assertThat(januaryPrediction.getHomeRatingBefore())
        .isEqualByComparingTo(Constant.baseEloFor(matchA.getCompetition()));
  }
}
