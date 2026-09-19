package com.bookie.event;

import java.util.List;

public record RatingUpdatedEvent(List<Long> teamIds) {}
