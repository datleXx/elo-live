package com.bookie.engine;

public class EloEngine {
  private static final int SCALE = 400;

  private final int K;
  private final int HOME_BIAS;

  public EloEngine(int k, int bias) {
    K = k;
    HOME_BIAS = bias;
  }

  public double calGoalDiffMultiplier(int homeGoal, int awayGoal) {
    int absGD = Math.abs(homeGoal - awayGoal);

    switch (absGD) {
      case 0, 1 -> {
        return 1;
      }
      case 2 -> {
        return 1.5;
      }
      default -> {
        return (11.0 + absGD) / 8;
      }
    }
  }

  public ExpectedScores calExpectedScores(double ratingHome, double ratingAway) {
    double ratingHomeEffective = ratingHome + HOME_BIAS;
    double expectedHome = 1 / (1 + Math.pow(10, -(ratingHomeEffective - ratingAway) / SCALE));
    double expectedAway = 1 - expectedHome;

    return new ExpectedScores(expectedHome, expectedAway);
  }

  private double calUpdatedRating(double oldRating, double gd, double actual, double expected) {
    return Math.round((oldRating + K * gd * (actual - expected)) * 100) / 100.0;
  }

  public Elo calElos(int homeGoal, int awayGoal, double ratingHome, double ratingAway) {
    double gdMul = calGoalDiffMultiplier(homeGoal, awayGoal);
    double actual = homeGoal > awayGoal ? 1 : awayGoal > homeGoal ? 0 : 0.5;
    ExpectedScores expectedScores = calExpectedScores(ratingHome, ratingAway);

    double updatedAwayRating =
        calUpdatedRating(ratingAway, gdMul, 1 - actual, expectedScores.away());
    double updatedHomeRating = calUpdatedRating(ratingHome, gdMul, actual, expectedScores.home());

    return new Elo(updatedHomeRating, updatedAwayRating);
  }
}
