package com.bookie.service;

import com.bookie.dto.RatingUpdateMessage;
import com.bookie.event.RatingUpdatedEvent;
import com.bookie.repository.RatingRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PublishRatingListener {
  private final RatingRepository ratingRepo;
  private final SimpMessagingTemplate messagingTemplate;

  public PublishRatingListener(
      RatingRepository ratingRepository, SimpMessagingTemplate messagingTemplate) {
    ratingRepo = ratingRepository;
    this.messagingTemplate = messagingTemplate;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onRatingUpdated(RatingUpdatedEvent event) {
    if (event.teamIds().isEmpty()) return;

    ratingRepo
        .findRatingUpdateMessages(event.teamIds())
        .forEach(
            message ->
                messagingTemplate.convertAndSend(
                    "/topic/ratings/" + message.competition(), message));
  }
}
