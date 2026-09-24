package com.bookie.controller;

import com.bookie.model.Prediction;
import com.bookie.repository.PredictionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PredictionController {
  private final PredictionRepository predictionRepo;

  public PredictionController(PredictionRepository predictionRepository) {
    predictionRepo = predictionRepository;
  }

  @GetMapping("/api/predictions/{competition}")
  public List<Prediction> predictions(@PathVariable String competition) {
    return predictionRepo.findForCompetition(competition);
  }
}
