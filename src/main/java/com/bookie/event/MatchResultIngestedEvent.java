package com.bookie.event;

import java.util.List;

public record MatchResultIngestedEvent(List<Long> matchIds) {}
