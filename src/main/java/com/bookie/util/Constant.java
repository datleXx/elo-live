package com.bookie.util;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public class Constant {
  public static final Set<String> ALLOWED_LEAGUES =
      Set.of(
          "E0",
          "E1",
          "E2",
          "E3",
          "EC", // England: Premier League -> National League
          "SP1",
          "SP2", // Spain: La Liga, Segunda División
          "D1",
          "D2", // Germany: Bundesliga, 2. Bundesliga
          "I1",
          "I2", // Italy: Serie A, Serie B
          "F1",
          "F2" // France: Ligue 1, Ligue 2
          );

  public static final Map<String, BigDecimal> LEAGUE_BASE_ELO_MAP =
      Map.ofEntries(
          Map.entry("E0", BigDecimal.valueOf(1700)),
          Map.entry("E1", BigDecimal.valueOf(1600)),
          Map.entry("E2", BigDecimal.valueOf(1500)),
          Map.entry("E3", BigDecimal.valueOf(1400)),
          Map.entry("EC", BigDecimal.valueOf(1300)),
          Map.entry("SP1", BigDecimal.valueOf(1700)),
          Map.entry("SP2", BigDecimal.valueOf(1550)),
          Map.entry("D1", BigDecimal.valueOf(1700)),
          Map.entry("D2", BigDecimal.valueOf(1550)),
          Map.entry("I1", BigDecimal.valueOf(1700)),
          Map.entry("I2", BigDecimal.valueOf(1550)),
          Map.entry("F1", BigDecimal.valueOf(1700)),
          Map.entry("F2", BigDecimal.valueOf(1550)));

  private static final String REPLAY_SUFFIX = "_REPLAY";

  public static BigDecimal baseEloFor(String competition) {
    String realCompetition =
        competition.endsWith(REPLAY_SUFFIX)
            ? competition.substring(0, competition.length() - REPLAY_SUFFIX.length())
            : competition;
    return LEAGUE_BASE_ELO_MAP.getOrDefault(realCompetition, BigDecimal.valueOf(1500));
  }
}
