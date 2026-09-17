package com.bookie.service;

import com.bookie.repository.MatchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;

@SpringBootTest
@Transactional
public class PredictionServiceTest {
  @Autowired private CsvIngestService ingestService;
  @Autowired private RatingService ratingService;
  @Autowired private PredictionService predictionService;
  @Autowired private MatchRepository matchRepository;

  @Test
  void backtestReportTest() throws IOException {
    Path csvFile = new ClassPathResource("data/E0_2324.csv").getFile().toPath();
    ingestService.ingest(csvFile, "E0");
    ratingService.backTestByCompetition("E0");

    BacktestReport report = predictionService.evaluateAccuracy("E0");

    System.out.println("Model correct:   " + report.modelCorrectCount());
    System.out.println("Market correct:  " + report.marketCorrectCount());
    System.out.println("Total fixtures:  " + report.totalFixtures());
    System.out.println("Disagreements:   " + report.disagreementList().size());
  }
}
