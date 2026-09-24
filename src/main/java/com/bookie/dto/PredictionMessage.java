package com.bookie.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PredictionMessage(
    String competition,
    LocalDate matchDate,
    String homeTeam,
    String awayTeam,
    BigDecimal homeWinProb,
    BigDecimal drawProb,
    BigDecimal awayWinProb,
    BigDecimal marketHomeWinProb,
    BigDecimal marketDrawProb,
    BigDecimal marketAwayWinProb) {}
