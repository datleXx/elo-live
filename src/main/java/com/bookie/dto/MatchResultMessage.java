package com.bookie.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MatchResultMessage(
    String competition,
    LocalDate matchDate,
    String homeTeam,
    String awayTeam,
    Integer homeGoals,
    Integer awayGoals,
    BigDecimal homeRatingBefore,
    BigDecimal awayRatingBefore,
    BigDecimal homeRatingAfter,
    BigDecimal awayRatingAfter) {}
