package com.bettercontent.seasonaldaylight.clock;

/**
 * Pure simulation-tick to sky-time integrator. One day and one night produce exactly
 * 24,000 logical dayTime ticks in cycleTicks server ticks; gameTime is never touched.
 * The saved state is a bounded phase counter, not a wall-clock or material history.
 */
public final class ClockAccumulator {
    private static final int HALF_DAY = 12_000;
    private static final int LOGICAL_DAY = 24_000;
    private final int cycleTicks;
    private long lastOutput;
    private int dayTicks;
    private int elapsedPhaseTicks;
    private boolean dayPhase;

    public record State(long lastOutput, int cycleTicks, int dayTicks, int elapsedPhaseTicks, boolean dayPhase) {}

    public ClockAccumulator(long observedDayTime, int cycleTicks, int initialDayTicks) {
        checkLengths(cycleTicks, initialDayTicks);
        this.cycleTicks = cycleTicks;
        this.dayTicks = initialDayTicks;
        rebase(observedDayTime, initialDayTicks);
    }

    private ClockAccumulator(State state) {
        checkLengths(state.cycleTicks(), state.dayTicks());
        int duration = state.dayPhase() ? state.dayTicks() : state.cycleTicks() - state.dayTicks();
        boolean observedDay = Math.floorMod(state.lastOutput(), LOGICAL_DAY) < HALF_DAY;
        if (state.elapsedPhaseTicks() < 0 || state.elapsedPhaseTicks() >= duration
                || observedDay != state.dayPhase()) throw new IllegalArgumentException("Invalid saved sky phase");
        this.cycleTicks = state.cycleTicks();
        this.lastOutput = state.lastOutput();
        this.dayTicks = state.dayTicks();
        this.elapsedPhaseTicks = state.elapsedPhaseTicks();
        this.dayPhase = state.dayPhase();
    }

    public static ClockAccumulator restore(State state) { return new ClockAccumulator(state); }
    public State snapshot() { return new State(lastOutput, cycleTicks, dayTicks, elapsedPhaseTicks, dayPhase); }
    public int cycleTicks() { return cycleTicks; }
    public int dayTicks() { return dayTicks; }

    /**
     * Pass the dayTime BEFORE vanilla's attempted +1, the daylight gamerule, and
     * the candidate sampled from Serene Seasons. Only sunrise or an external time
     * command/sleep jump latches a new day's target; ordinary calls are monotone.
     */
    public long advance(long observedDayTime, boolean enabled, int candidateDayTicks) {
        checkLengths(cycleTicks, candidateDayTicks);
        if (observedDayTime != lastOutput) rebase(observedDayTime, candidateDayTicks);
        if (!enabled) return observedDayTime;
        if (dayPhase && elapsedPhaseTicks == 0 && Math.floorMod(observedDayTime, LOGICAL_DAY) == 0)
            dayTicks = candidateDayTicks;
        int duration = dayPhase ? dayTicks : cycleTicks - dayTicks;
        int currentOffset = Math.floorMod(observedDayTime, LOGICAL_DAY) % HALF_DAY;
        int nextElapsed = Math.min(duration, elapsedPhaseTicks + 1);
        int desiredOffset = (int) ((long) nextElapsed * HALF_DAY / duration);
        // After a /time command the first target may be ahead of the arbitrary
        // commanded offset, but it must never rewind the observed clock.
        long nextTime = observedDayTime + Math.max(0, desiredOffset - currentOffset);
        if (nextElapsed == duration) {
            // The integer schedule reaches the exact boundary on its final tick.
            // Any external jump was rebased before this call.
            dayPhase = !dayPhase;
            elapsedPhaseTicks = 0;
        } else elapsedPhaseTicks = nextElapsed;
        lastOutput = nextTime;
        return nextTime;
    }

    private void rebase(long observed, int candidateDayTicks) {
        lastOutput = observed;
        dayTicks = candidateDayTicks;
        int offset = Math.floorMod(observed, LOGICAL_DAY);
        dayPhase = offset < HALF_DAY;
        int phaseOffset = offset % HALF_DAY;
        int duration = dayPhase ? dayTicks : cycleTicks - dayTicks;
        // At an existing-world load or external jump align to its current sky
        // angle without rewriting time; the first partial phase may be rounded
        // once, then every subsequent complete phase has exact fixed duration.
        elapsedPhaseTicks = (int) Math.min(duration - 1L,
                ((long) phaseOffset * duration + HALF_DAY - 1L) / HALF_DAY);
    }

    private static void checkLengths(int cycleTicks, int dayTicks) {
        if (cycleTicks < 2 || dayTicks < 1 || dayTicks >= cycleTicks)
            throw new IllegalArgumentException("Day and night must have positive duration");
    }
}
