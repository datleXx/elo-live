package com.bookie.service;

import com.bookie.repository.RatingRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class RatingLookupService {
  private final RatingRepository ratingRepo;

  public RatingLookupService(RatingRepository ratingRepository) {
    ratingRepo = ratingRepository;
  }

  @Cacheable(cacheNames = "ratingAt", key = "#teamId + ':' + #date")
  public BigDecimal ratingAt(Long teamId, LocalDate date) {
    return ratingRepo.findFirstByTeamIdBeforeDate(teamId, date).orElse(BigDecimal.valueOf(1500));
  }
}
