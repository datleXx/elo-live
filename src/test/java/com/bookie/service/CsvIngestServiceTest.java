package com.bookie.service;

import com.bookie.repository.MatchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CsvIngestServiceTest {

  @Autowired private CsvIngestService ingestService;

  @Autowired private MatchRepository matchRepository;

  @Test
  void ingestIsIdempotent() throws IOException {
    Path csvFile = new ClassPathResource("data/E0_2324.csv").getFile().toPath();

    ingestService.ingest(csvFile, "E0");
    long count = matchRepository.count();

    assertThat(count).isGreaterThan(0);
    ingestService.ingest(csvFile, "E0");

    assertThat(matchRepository.count()).isEqualTo(count);
  }
}
