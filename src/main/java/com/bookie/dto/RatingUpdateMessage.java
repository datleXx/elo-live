package com.bookie.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RatingUpdateMessage(
    String competition, Long teamId, String teamName, BigDecimal rating, LocalDate matchDate) {}
