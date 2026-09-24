package com.bookie.service;

import com.bookie.event.RatingUpdatedEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PublishRatingListener {
  private final SimpMessagingTemplate messagingTemplate;

  public PublishRatingListener(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  // Pushes exactly what UpdateRatingListener just computed and committed -
  // no re-querying "the latest rating" here, so there's nothing left that
  // could pick up a different competition's data by mistake.
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onRatingUpdated(RatingUpdatedEvent event) {
    event
        .results()
        .forEach(
            result ->
                messagingTemplate.convertAndSend(
                    "/topic/ratings/" + result.competition(), result));
  }
}
