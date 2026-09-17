package com.bookie.engine;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class EloEngineTest {

    @Test
    void evenlyMatchedTeamsWithHomeAdvantage() {
        EloEngine engine = new EloEngine(20, 100);
        Elo result = engine.calElos(2, 0, 1500, 1500);

        assertThat(result.home()).isCloseTo(1510.80, within(0.01));
        assertThat(result.away()).isCloseTo(1489.20, within(0.01));
    }

    @Test
    void drawBetweenMismatchedTeams() {
        EloEngine engine = new EloEngine(20, 100);
        Elo result = engine.calElos(1, 1, 1600, 1400);

        assertThat(result.home()).isCloseTo(1593.02, within(0.01));
        assertThat(result.away()).isCloseTo(1406.98, within(0.01));
    }

    @Test
    void bigUpset() {
        EloEngine engine = new EloEngine(20, 100);
        Elo result = engine.calElos(0, 4, 1400, 1700);

        assertThat(result.home()).isCloseTo(1390.99, within(0.01));
        assertThat(result.away()).isCloseTo(1709.01, within(0.01));
    }
}
