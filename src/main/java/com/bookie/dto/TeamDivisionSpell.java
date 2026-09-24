package com.bookie.dto;

import java.time.LocalDate;

public record TeamDivisionSpell(String competition, LocalDate firstMatch, LocalDate lastMatch) {}
