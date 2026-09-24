package com.bookie.controller;

import com.bookie.dto.RatingUpdateMessage;
import com.bookie.repository.RatingRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RatingController {
  private final RatingRepository ratingRepo;

  public RatingController(RatingRepository ratingRepository) {
    ratingRepo = ratingRepository;
  }

  @GetMapping("/api/ratings/{competition}")
  public List<RatingUpdateMessage> currentRatings(@PathVariable String competition) {
    return ratingRepo.findCurrentRatingsForCompetition(competition);
  }
}
