package com.bookie.service;

import com.bookie.model.Match;
import com.bookie.model.MatchKey;
import com.bookie.model.MatchResult;
import com.bookie.model.Team;
import com.bookie.repository.MatchRepository;
import com.bookie.repository.TeamRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;

@Service
public class CsvIngestService {
  private final TeamRepository teamRepo;
  private final MatchRepository matchRepo;

  private static final DateTimeFormatter DATE_4Y = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter DATE_2Y =
      new DateTimeFormatterBuilder()
          .appendPattern("dd/MM/")
          .appendValueReduced(ChronoField.YEAR, 2, 2, 1950)
          .toFormatter();

  private static LocalDate parseMatchDate(String raw) {
    return raw.length() == 10 ? LocalDate.parse(raw, DATE_4Y) : LocalDate.parse(raw, DATE_2Y);
  }

  private static BigDecimal parseOdds(CSVRecord record, String column) {
    if (!record.isMapped(column)) {
      return null;
    }
    String value = record.get(column);
    if (value.isBlank()) {
      return null;
    }
    return new BigDecimal(value);
  }

  public CsvIngestService(TeamRepository teamRepo, MatchRepository matchRepo) {
    this.teamRepo = teamRepo;
    this.matchRepo = matchRepo;
  }

  @Transactional
  public void ingest(Path csvFile, String competition) throws IOException {
    List<Team> allTeams = teamRepo.findAll();
    List<Match> matchesByCompetition = matchRepo.findByCompetition(competition);

    Map<String, Team> teamMap = new HashMap<>();
    Set<MatchKey> matchKeySet = new HashSet<>();

    for (Match match : matchesByCompetition) {
      MatchKey curr =
          new MatchKey(
              match.getCompetition(),
              match.getMatchDate(),
              match.getHomeTeam().getId(),
              match.getAwayTeam().getId());

      matchKeySet.add(curr);
    }

    for (Team team : allTeams) {
      teamMap.put(team.getName(), team);
    }

    try (Reader reader = Files.newBufferedReader(csvFile);
        CSVParser parser =
            CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(reader)) {

      List<Match> batchList = new ArrayList<>();

      for (CSVRecord record : parser) {
        String homeTeamName = record.get("HomeTeam");
        String awayTeamName = record.get("AwayTeam");
        String matchDateString = record.get("Date");

        if (homeTeamName.isBlank() || awayTeamName.isBlank() || matchDateString.isBlank()) continue;

        Team homeTeam = teamMap.get(homeTeamName);
        Team awayTeam = teamMap.get(awayTeamName);

        if (homeTeam == null) {
          homeTeam = teamRepo.save(new Team(homeTeamName));
          teamMap.put(homeTeamName, homeTeam);
        }

        if (awayTeam == null) {
          awayTeam = teamRepo.save(new Team(awayTeamName));
          teamMap.put(awayTeamName, awayTeam);
        }

        LocalDate matchDate = parseMatchDate(matchDateString);
        MatchKey matchKey =
            new MatchKey(competition, matchDate, homeTeam.getId(), awayTeam.getId());
        if (matchKeySet.contains(matchKey)) continue;

        matchKeySet.add(matchKey);

        int fullTimeHomeGoals = Integer.parseInt(record.get("FTHG"));
        int fullTimeAwayGoals = Integer.parseInt(record.get("FTAG"));
        MatchResult fullTimeResult =
            fullTimeAwayGoals > fullTimeHomeGoals
                ? MatchResult.A
                : fullTimeHomeGoals > fullTimeAwayGoals ? MatchResult.H : MatchResult.D;

        Match toSave =
            new Match(
                competition,
                matchDate,
                homeTeam,
                awayTeam,
                fullTimeHomeGoals,
                fullTimeAwayGoals,
                fullTimeResult,
                parseOdds(record, "B365H"),
                parseOdds(record, "B365D"),
                parseOdds(record, "B365A"));

        batchList.add(toSave);
      }

      matchRepo.saveAll(batchList);
    }
  }
}
