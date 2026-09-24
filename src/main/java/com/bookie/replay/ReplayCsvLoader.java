package com.bookie.replay;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

import static com.bookie.util.CsvParsing.parseMatchDate;
import static com.bookie.util.CsvParsing.parseOdds;

@Component
public class ReplayCsvLoader {
  public List<List<ReplayRow>> load(Path csvFile) throws IOException {
    Map<LocalDate, List<ReplayRow>> mapByDate = new TreeMap<>();
    try (Reader reader = Files.newBufferedReader(csvFile);
        CSVParser parser =
            CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setAllowMissingColumnNames(true)
                .setSkipHeaderRecord(true)
                .get()
                .parse(reader)) {

      for (CSVRecord record : parser) {
        String homeTeamName = record.get("HomeTeam");
        String awayTeamName = record.get("AwayTeam");
        String matchDateString = record.get("Date");

        if (homeTeamName.isBlank() || awayTeamName.isBlank() || matchDateString.isBlank()) continue;

        LocalDate matchDate = parseMatchDate(matchDateString);
        int fullTimeHomeGoals = Integer.parseInt(record.get("FTHG"));
        int fullTimeAwayGoals = Integer.parseInt(record.get("FTAG"));

        ReplayRow replayRow =
            new ReplayRow(
                matchDate,
                homeTeamName,
                awayTeamName,
                fullTimeHomeGoals,
                fullTimeAwayGoals,
                parseOdds(record, "B365H"),
                parseOdds(record, "B365D"),
                parseOdds(record, "B365A"));

        mapByDate.computeIfAbsent(matchDate, k -> new ArrayList<>()).add(replayRow);
      }
    }

    return new ArrayList<>(mapByDate.values());
  }
}
