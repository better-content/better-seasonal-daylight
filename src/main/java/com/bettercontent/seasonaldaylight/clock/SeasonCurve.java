package com.bettercontent.seasonaldaylight.clock;

/** Pure northern-hemisphere annual solar schedule, independent of Minecraft or SS classes. */
public final class SeasonCurve {
    public static final double MIN_DAY_FRACTION = 5.0 / 9.0;
    private SeasonCurve() {}

    /**
     * Serene Seasons orders 12 equal sub-seasons starting with EARLY_SPRING.
     * Mid-spring/equinox is at 1.5/12, mid-summer/solstice at 4.5/12,
     * mid-autumn/equinox at 7.5/12 and mid-winter/solstice at 10.5/12.
     */
    public static double dayFraction(double annualPhase, double equinoxFraction, double amplitude) {
        if (!Double.isFinite(annualPhase) || !Double.isFinite(equinoxFraction) || !Double.isFinite(amplitude)
                || equinoxFraction <= 0 || equinoxFraction >= 1 || amplitude < 0)
            throw new IllegalArgumentException("Invalid annual solar policy");
        double fraction = equinoxFraction + amplitude * Math.cos(2 * Math.PI * (annualPhase - .375));
        return Math.max(MIN_DAY_FRACTION, Math.min(.99, fraction));
    }

    /** Rounded once per sunrise: day + night always equals the configured fixed cycle. */
    public static int dayTicks(int cycleTicks, double annualPhase, double equinoxFraction, double amplitude) {
        if (cycleTicks < 3) throw new IllegalArgumentException("Cycle must fit both phases with night <= 80% of day");
        long rounded = Math.round(cycleTicks * dayFraction(annualPhase, equinoxFraction, amplitude));
        // Ceil(5 * cycle / 9), not rounding: even one tick below violates the night/day bound.
        long minimumDay = (5L * cycleTicks + 8L) / 9L;
        return (int) Math.max(minimumDay, Math.min(cycleTicks - 1L, rounded));
    }
}
