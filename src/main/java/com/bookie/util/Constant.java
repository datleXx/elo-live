package com.bookie.util;

import java.util.Set;

public class Constant {
  public static final Set<String> ALLOWED_LEAGUES =
      Set.of(
          "E0", "E1", "E2", "E3", "EC", // England: Premier League -> National League
          "SP1", "SP2", // Spain: La Liga, Segunda División
          "D1", "D2", // Germany: Bundesliga, 2. Bundesliga
          "I1", "I2", // Italy: Serie A, Serie B
          "F1", "F2" // France: Ligue 1, Ligue 2
          );
}
