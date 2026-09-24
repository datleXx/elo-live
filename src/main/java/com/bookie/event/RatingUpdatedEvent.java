package com.bookie.event;

import com.bookie.dto.MatchResultMessage;

import java.util.List;

public record RatingUpdatedEvent(List<Long> teamIds, List<MatchResultMessage> results) {}
