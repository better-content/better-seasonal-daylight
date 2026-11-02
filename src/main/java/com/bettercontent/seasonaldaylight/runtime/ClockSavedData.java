package com.bettercontent.seasonaldaylight.runtime;

import com.bettercontent.seasonaldaylight.clock.ClockAccumulator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** One small Overworld phase record; never stores a replacement calendar or player/world data. */
final class ClockSavedData extends SavedData {
    private static final String KEY = "better_seasonal_daylight_clock";
    private ClockAccumulator clock;

    static ClockSavedData of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ClockSavedData::load, ClockSavedData::new, KEY);
    }

    ClockAccumulator clock() { return clock; }
    void setClock(ClockAccumulator value) { clock = value; setDirty(); }

    private static ClockSavedData load(CompoundTag tag) {
        ClockSavedData data = new ClockSavedData();
        if (tag.getInt("version") != 1 || !tag.contains("lastOutput", Tag.TAG_LONG)
                || !tag.contains("cycleTicks", Tag.TAG_INT) || !tag.contains("dayTicks", Tag.TAG_INT)
                || !tag.contains("elapsedPhaseTicks", Tag.TAG_INT) || !tag.contains("dayPhase", Tag.TAG_BYTE))
            return data;
        try {
            data.clock = ClockAccumulator.restore(new ClockAccumulator.State(
                    tag.getLong("lastOutput"), tag.getInt("cycleTicks"), tag.getInt("dayTicks"),
                    tag.getInt("elapsedPhaseTicks"), tag.getBoolean("dayPhase")));
        } catch (IllegalArgumentException invalid) {
            // Invalid own data is ignored. The real world dayTime is never overwritten.
        }
        return data;
    }

    @Override public CompoundTag save(CompoundTag tag) {
        if (clock == null) return tag;
        ClockAccumulator.State state = clock.snapshot();
        tag.putInt("version", 1);
        tag.putLong("lastOutput", state.lastOutput());
        tag.putInt("cycleTicks", state.cycleTicks());
        tag.putInt("dayTicks", state.dayTicks());
        tag.putInt("elapsedPhaseTicks", state.elapsedPhaseTicks());
        tag.putBoolean("dayPhase", state.dayPhase());
        return tag;
    }
}
