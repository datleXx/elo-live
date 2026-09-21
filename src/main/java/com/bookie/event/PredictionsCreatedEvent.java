package com.bookie.event;

import java.util.List;

public record PredictionsCreatedEvent(List<Long> predictionIds) {}
