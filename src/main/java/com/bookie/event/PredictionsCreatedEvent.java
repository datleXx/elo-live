package com.bookie.event;

import com.bookie.dto.PredictionMessage;

import java.util.List;

public record PredictionsCreatedEvent(List<PredictionMessage> predictions) {}
