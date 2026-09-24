package com.bookie.service;

import com.bookie.event.PredictionsCreatedEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PublishPredictionListener {
  private final SimpMessagingTemplate messagingTemplate;

  public PublishPredictionListener(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onPredictionCreated(PredictionsCreatedEvent event) {
    event
        .predictions()
        .forEach(
            prediction ->
                messagingTemplate.convertAndSend(
                    "/topic/predictions/" + prediction.competition(), prediction));
  }
}
