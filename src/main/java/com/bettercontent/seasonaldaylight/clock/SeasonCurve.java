package com.bettercontent.seasonaldaylight.clock;

/** Pure northern-hemisphere annual solar schedule, independent of Minecraft or SS classes. */
public final class SeasonCurve {
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
        return Math.max(.01, Math.min(.99, fraction));
    }

    /** Rounded once per sunrise: day + night always equals the configured fixed cycle. */
    public static int dayTicks(int cycleTicks, double annualPhase, double equinoxFraction, double amplitude) {
        if (cycleTicks < 2) throw new IllegalArgumentException("Cycle must contain both phases");
        long rounded = Math.round(cycleTicks * dayFraction(annualPhase, equinoxFraction, amplitude));
        return (int) Math.max(1, Math.min(cycleTicks - 1L, rounded));
    }
}
