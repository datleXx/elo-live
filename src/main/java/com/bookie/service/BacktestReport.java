package com.bookie.service;

import com.bookie.model.Match;

import java.util.List;

public record BacktestReport(
    int modelCorrectCount,
    int marketCorrectCount,
    int totalFixtures,
    List<Match> disagreementList) {}
