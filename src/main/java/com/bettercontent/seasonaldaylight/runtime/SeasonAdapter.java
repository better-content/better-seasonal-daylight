package com.bettercontent.seasonaldaylight.runtime;

import net.minecraft.server.level.ServerLevel;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.SeasonHelper;

/** Read the actual northern annual SS clock; never reset or advance it here. */
final class SeasonAdapter {
    private SeasonAdapter() {}

    static double annualPhase(ServerLevel level) {
        ISeasonState state = SeasonHelper.getSeasonState(level);
        if (state == null || state.getCycleDuration() <= 0) return .125; // equinox fallback
        return Math.floorMod(state.getSeasonCycleTicks(), state.getCycleDuration())
                / (double) state.getCycleDuration();
    }
}
