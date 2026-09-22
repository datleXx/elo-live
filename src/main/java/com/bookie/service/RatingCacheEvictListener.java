package com.bookie.service;

import com.bookie.event.RatingUpdatedEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RatingCacheEvictListener {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @CacheEvict(cacheNames = "ratingAt", allEntries = true)
    public void onRatingUpdated(RatingUpdatedEvent event) {}
}
