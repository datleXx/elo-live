package com.bookie.controller;

import com.bookie.replay.ReplayRunner;
import com.bookie.validator.ValidSeason;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.bookie.util.Constant.ALLOWED_LEAGUES;

@RestController
public class ReplayController {
  private final ReplayRunner replayRunner;

  public ReplayController(ReplayRunner replayRunner) {
    this.replayRunner = replayRunner;
  }

  @PostMapping("/api/replay/start")
  public ResponseEntity<String> start(
      @RequestParam String league,
      @RequestParam @ValidSeason String season,
      @RequestParam(defaultValue = "2000") @Positive long delayMillis) {
    if (!ALLOWED_LEAGUES.contains(league))
      return ResponseEntity.badRequest().body("League not in the allowed list");
    boolean status = replayRunner.start(league, season, delayMillis);
    if (status) return ResponseEntity.status(202).body("Accepted");
    return ResponseEntity.status(409)
        .body("Another process is currently replaying, try again later");
  }

  @PostMapping("/api/replay/stop")
  public ResponseEntity<String> stop() {
    replayRunner.stop();
    return ResponseEntity.ok("Stopped");
  }

  @GetMapping("/api/replay/status")
  public ResponseEntity<Boolean> status() {
    return ResponseEntity.ok(replayRunner.isRunning());
  }
}
