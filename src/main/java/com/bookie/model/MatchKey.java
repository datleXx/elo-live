package com.bookie.model;

import java.time.LocalDate;

public record MatchKey(String competition, LocalDate matchDate, Long homeTeamId, Long awayTeamId) {}
