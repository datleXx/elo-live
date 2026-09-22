package com.bookie.controller;

import com.bookie.service.CsvIngestService;
import com.bookie.service.DownloadCsvService;
import com.bookie.validator.ValidSeason;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@RestController
public class IngestController {
  private final DownloadCsvService downloadCsvService;
  private final CsvIngestService csvIngestService;
  private static final Set<String> ALLOWED_LEAGUES = Set.of("E0", "SP1", "D1", "I1", "F1");

  public IngestController(
      DownloadCsvService downloadCsvService, CsvIngestService csvIngestService) {
    this.downloadCsvService = downloadCsvService;
    this.csvIngestService = csvIngestService;
  }

  @PostMapping("/api/ingest")
  public ResponseEntity<String> ingest(
      @RequestParam String league, @RequestParam @ValidSeason String season) throws IOException {
    if (!ALLOWED_LEAGUES.contains(league))
      return ResponseEntity.badRequest().body("League not in the allowed list");
    Path temp = downloadCsvService.download(season, league);

    try {
      csvIngestService.ingest(temp, league);
    } finally {
      Files.deleteIfExists(temp);
    }

    return ResponseEntity.ok("Ingest done!");
  }
}
