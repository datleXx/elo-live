package com.bookie.replay;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReplayRow(
    LocalDate date,
    String homeTeam,
    String awayTeam,
    int homeGoals,
    int awayGoals,
    BigDecimal homeOdds,
    BigDecimal drawOdds,
    BigDecimal awayOdds) {}
