package com.bettercontent.seasonaldaylight.clock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pure annual sunlight policy; does not load Forge, a world or Serene Seasons. */
class SeasonCurveTest {
    private static final int CYCLE = 50_000;
    private static final double EQUINOX = .5;
    private static final double SWING = .2;

    @Test
    void trueSereneSeasonsMidpointAnchors() {
        // SeasonTime sub-season order: early/mid/late spring, summer, autumn, winter.
        // The anchor lies at the CENTER of each middle sub-season, not its first day.
        assertEquals(.5, SeasonCurve.dayFraction(.125, EQUINOX, SWING), 1e-12);
        assertEquals(.7, SeasonCurve.dayFraction(.375, EQUINOX, SWING), 1e-12);
        assertEquals(.5, SeasonCurve.dayFraction(.625, EQUINOX, SWING), 1e-12);
        assertEquals(.3, SeasonCurve.dayFraction(.875, EQUINOX, SWING), 1e-12);
        assertEquals(25_000, SeasonCurve.dayTicks(CYCLE, .125, EQUINOX, SWING));
        assertEquals(35_000, SeasonCurve.dayTicks(CYCLE, .375, EQUINOX, SWING));
        assertEquals(15_000, SeasonCurve.dayTicks(CYCLE, .875, EQUINOX, SWING));
    }

    @Test
    void annualWrapIsContinuousAndBoundsEveryDailySample() {
        assertEquals(SeasonCurve.dayFraction(0, EQUINOX, SWING),
                SeasonCurve.dayFraction(1, EQUINOX, SWING), 1e-12);
        assertEquals(SeasonCurve.dayFraction(.375, EQUINOX, SWING),
                SeasonCurve.dayFraction(1.375, EQUINOX, SWING), 1e-12);
        for (int day = 0; day < 12 * 64; day++) {
            double phase = (day + .5) / (12.0 * 64);
            double fraction = SeasonCurve.dayFraction(phase, EQUINOX, SWING);
            int daylight = SeasonCurve.dayTicks(CYCLE, phase, EQUINOX, SWING);
            assertTrue(fraction >= .3 - 1e-12 && fraction <= .7 + 1e-12, "phase=" + phase);
            assertTrue(daylight >= 15_000 && daylight <= 35_000, "phase=" + phase);
            assertEquals(CYCLE, daylight + (CYCLE - daylight), "no variable full-cycle length");
        }
    }

    @Test
    void riseAndFallAreSmoothThroughAllFourQuarterSeasons() {
        double previous = SeasonCurve.dayFraction(.125, EQUINOX, SWING);
        for (int step = 1; step <= 250; step++) {
            double phase = .125 + .25 * step / 250.0;
            double next = SeasonCurve.dayFraction(phase, EQUINOX, SWING);
            assertTrue(next >= previous - 1e-12, "spring to summer: " + phase);
            previous = next;
        }
        for (int step = 1; step <= 500; step++) {
            double phase = .375 + .5 * step / 500.0;
            double next = SeasonCurve.dayFraction(phase, EQUINOX, SWING);
            assertTrue(next <= previous + 1e-12, "summer to winter: " + phase);
            previous = next;
        }
    }

    @Test
    void zeroAmplitudeIsOrdinaryEquinoxPolicyNotAnExtraSeasonalMultiplier() {
        for (double phase : new double[]{0, .125, .375, .625, .875, 1}) {
            assertEquals(.5, SeasonCurve.dayFraction(phase, .5, 0), 1e-12);
            assertEquals(25_000, SeasonCurve.dayTicks(CYCLE, phase, .5, 0));
        }
    }
}
