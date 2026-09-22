package com.bookie.service;

import com.bookie.model.Match;
import com.bookie.model.Prediction;
import com.bookie.model.Rating;
import com.bookie.model.Team;
import com.bookie.repository.MatchRepository;
import com.bookie.repository.PredictionRepository;
import com.bookie.repository.RatingRepository;
import com.bookie.repository.TeamRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * No @Transactional here on purpose: @TransactionalEventListener(AFTER_COMMIT) only fires after a
 * real commit, and a @Transactional test never commits, it only rolls back. So this test lets the
 * ingest commit for real, then cleans up explicitly in @AfterEach.
 */
@SpringBootTest
class EventPipelineTest {

  private static final String COMPETITION = "TESTCOMP";

  @Autowired private TeamRepository teamRepo;
  @Autowired private MatchRepository matchRepo;
  @Autowired private RatingRepository ratingRepo;
  @Autowired private PredictionRepository predictionRepo;
  @Autowired private CsvIngestService ingestService;

  private static final List<String> TEST_TEAM_NAMES =
      List.of("Test Home FC", "Test Away FC", "Test Opponent FC");

  @Test
  void ingestingAResultCascadesToRatingsAndUpcomingPredictions() throws IOException {
    Team home = teamRepo.save(new Team("Test Home FC"));
    Team away = teamRepo.save(new Team("Test Away FC"));
    Team opponent = teamRepo.save(new Team("Test Opponent FC"));

    Match upcomingFixture =
        matchRepo.save(
            new Match(
                COMPETITION,
                LocalDate.now().plusDays(7),
                home,
                opponent,
                null,
                null,
                null,
                null,
                null));

    Path csvFile = new ClassPathResource("data/event_pipeline_test.csv").getFile().toPath();
    ingestService.ingest(csvFile, COMPETITION);

    Optional<Rating> homeRating =
        ratingRepo.findFirstByTeamIdOrderByAsOfMatch_MatchDateDesc(home.getId());
    assertThat(homeRating).isPresent();
    assertThat(homeRating.get().getRating()).isNotEqualByComparingTo(BigDecimal.valueOf(1500));

    Optional<Prediction> upcomingPrediction = predictionRepo.findByMatchId(upcomingFixture.getId());
    assertThat(upcomingPrediction).isPresent();
    assertThat(upcomingPrediction.get().getHomeRatingBefore())
        .isEqualByComparingTo(homeRating.get().getRating());
  }

  @AfterEach
  void cleanUp() {
    List<Long> teamIds = teamRepo.findByNameIn(TEST_TEAM_NAMES).stream().map(Team::getId).toList();

    predictionRepo.deleteByMatch_Competition(COMPETITION);
    ratingRepo.deleteByTeamIdIn(teamIds);
    matchRepo.deleteAll(matchRepo.findByCompetition(COMPETITION));
    teamRepo.deleteAllById(teamIds);
  }
}
