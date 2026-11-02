package com.bettercontent.seasonaldaylight.clock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pure server sky-clock contract; never constructs a Level or starts a mod loader. */
class ClockAccumulatorTest {
    private static final int CYCLE = 50_000;

    @Test
    void fullEquinoxCycleUsesExactlyFiftyThousandSimulationTicks() {
        ClockAccumulator clock = new ClockAccumulator(0, CYCLE, 25_000);
        long time = 0;
        for (int tick = 1; tick <= CYCLE; tick++) {
            long next = clock.advance(time, true, 25_000);
            assertTrue(next >= time, "dayTime must never rewind at tick " + tick);
            if (tick == 25_000) assertEquals(12_000, next, "sunset tick");
            if (tick == CYCLE) assertEquals(24_000, next, "sunrise tick");
            time = next;
        }
        assertEquals(24_000, time);
        assertEquals(0, clock.snapshot().elapsedPhaseTicks());
        assertTrue(clock.snapshot().dayPhase());
    }

    @Test
    void summerAndWinterKeepTotalFixedWhileReallocatingTheSameWork() {
        for (int day : new int[]{15_000, 25_000, 35_000}) {
            ClockAccumulator clock = new ClockAccumulator(0, CYCLE, day);
            long time = 0;
            for (int tick = 1; tick <= CYCLE; tick++) {
                time = clock.advance(time, true, day);
                if (tick == day) assertEquals(12_000, time, "native sunset, dayTicks=" + day);
            }
            assertEquals(24_000, time, "fixed sunrise, dayTicks=" + day);
        }
    }

    @Test
    void nonDivisiblePhaseLengthsNeverAccumulateDayTimeRoundingDrift() {
        int cycle = 24_001;
        int day = 7_333;
        ClockAccumulator clock = new ClockAccumulator(0, cycle, day);
        long time = 0;
        for (int tick = 1; tick <= cycle * 4; tick++) {
            time = clock.advance(time, true, day);
            if (tick % cycle == day) assertEquals(12_000L * (tick / cycle * 2L + 1L), time);
            if (tick % cycle == 0) assertEquals(24_000L * (tick / cycle), time);
        }
    }

    @Test
    void latchesNewDayLengthAtSunriseNotDuringDayOrNight() {
        ClockAccumulator clock = new ClockAccumulator(0, CYCLE, 25_000);
        long time = 0;
        for (int tick = 1; tick <= CYCLE; tick++) {
            // Switch to winter after the first 5,000 ticks, well inside the summer day.
            time = clock.advance(time, true, tick <= 5_000 ? 35_000 : 15_000);
            if (tick == 35_000) assertEquals(12_000, time, "summer day must not be shortened mid-cycle");
            if (tick == CYCLE) assertEquals(24_000, time, "summer night completes original fixed cycle");
        }
        for (int tick = 1; tick <= 15_000; tick++) time = clock.advance(time, true, 15_000);
        assertEquals(36_000, time, "winter day begins only at next sunrise");
    }

    @Test
    void pausedCycleDoesNotBankFractionalProgress() {
        ClockAccumulator clock = new ClockAccumulator(0, CYCLE, 35_000);
        long time = 0;
        for (int i = 0; i < 11_999; i++) time = clock.advance(time, true, 35_000);
        ClockAccumulator.State before = clock.snapshot();
        for (int i = 0; i < 300; i++) assertEquals(time, clock.advance(time, false, 35_000));
        assertEquals(before, clock.snapshot(), "no elapsed simulated phase time while paused");
        long next = clock.advance(time, true, 35_000);
        assertTrue(next >= time);
        assertEquals(before.elapsedPhaseTicks() + 1, clock.snapshot().elapsedPhaseTicks());
    }

    @Test
    void restartCarriesOnlyBoundedPhaseAndMatchesUninterruptedClock() {
        ClockAccumulator live = new ClockAccumulator(0, CYCLE, 35_000);
        long time = 0;
        for (int i = 0; i < 38_721; i++) time = live.advance(time, true, 35_000);
        ClockAccumulator.State state = live.snapshot();
        ClockAccumulator restarted = ClockAccumulator.restore(state);
        assertEquals(state, restarted.snapshot());
        for (int i = 0; i < 62_000; i++) {
            long expected = live.advance(time, true, 15_000);
            long actual = restarted.advance(time, true, 15_000);
            assertEquals(expected, actual, "restart tick=" + i);
            time = expected;
        }
    }

    @Test
    void externalTimeCommandsAndSleepAreRespectedAndRebased() {
        ClockAccumulator clock = new ClockAccumulator(0, CYCLE, 35_000);
        long time = 0;
        for (int i = 0; i < 1_000; i++) time = clock.advance(time, true, 35_000);
        long commanded = 13000; // jump into night
        long afterCommand = clock.advance(commanded, true, 15_000);
        assertTrue(afterCommand >= commanded, "do not undo /time set");
        assertFalse(clock.snapshot().dayPhase(), "night phase rebased");
        long sleptToSunrise = 24_000;
        long afterSleep = clock.advance(sleptToSunrise, true, 25_000);
        assertTrue(afterSleep >= sleptToSunrise, "do not undo sleep");
        assertTrue(clock.snapshot().dayPhase(), "sunrise phase rebased");
        assertEquals(25_000, clock.dayTicks());
        long backwardCommand = 6_000;
        assertTrue(clock.advance(backwardCommand, true, 35_000) >= backwardCommand,
                "deliberate backward /time set must be respected, not confused with native clock rewind");
    }

    @Test
    void fasterThanOneLogicalTickPerSimulationTickIsNeverClampedAway() {
        ClockAccumulator clock = new ClockAccumulator(0, 24_000, 9_000);
        long time = 0;
        for (int tick = 1; tick <= 24_000; tick++) {
            long next = clock.advance(time, true, 9_000);
            assertTrue(next >= time, "monotone even at >1 sky tick per simulation tick");
            if (tick == 9_000) assertEquals(12_000, next);
            time = next;
        }
        assertEquals(24_000, time);
    }

    @Test
    void rejectsCorruptRestoredStateAndInvalidPhaseLengths() {
        assertThrows(IllegalArgumentException.class, () -> new ClockAccumulator(0, CYCLE, CYCLE));
        assertThrows(IllegalArgumentException.class, () -> new ClockAccumulator(0, CYCLE, 0));
        assertThrows(IllegalArgumentException.class, () -> ClockAccumulator.restore(
                new ClockAccumulator.State(100, CYCLE, 35_000, -1, true)));
        assertThrows(IllegalArgumentException.class, () -> ClockAccumulator.restore(
                new ClockAccumulator.State(13_000, CYCLE, 35_000, 0, true)));
    }
}
