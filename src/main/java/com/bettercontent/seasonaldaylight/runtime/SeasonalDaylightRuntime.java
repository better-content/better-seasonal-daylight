package com.bettercontent.seasonaldaylight.runtime;

import com.bettercontent.seasonaldaylight.SeasonalDaylightConfig;
import com.bettercontent.seasonaldaylight.clock.ClockAccumulator;
import com.bettercontent.seasonaldaylight.clock.SeasonCurve;
import com.bettercontent.seasonaldaylight.network.DaylightNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

/** Called only at vanilla's attempted dayTime write, after gameTime/timers have advanced. */
public final class SeasonalDaylightRuntime {
    private SeasonalDaylightRuntime() {}

    public static void register() {
        DaylightNetwork.register();
        MinecraftForge.EVENT_BUS.addListener(SeasonalDaylightRuntime::playerJoined);
    }

    private static void playerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) return;
        ClockAccumulator clock = ClockSavedData.of(level).clock();
        if (clock != null) DaylightNetwork.send(player, clock.snapshot());
        // Before the first tick, the periodic phase packet will arrive in at most 20 ticks.
    }

    public static long adjust(ServerLevel level, long vanillaNext) {
        if (level.dimension() != Level.OVERWORLD) return vanillaNext;
        long current = level.getDayTime();
        if (vanillaNext != current + 1) return vanillaNext; // never override another clock owner
        int cycle = Math.toIntExact(SeasonalDaylightConfig.CYCLE_TICKS.get());
        ClockSavedData saved = ClockSavedData.of(level);
        ClockAccumulator clock = saved.clock();
        // Keep the old cycle's candidate within its own bound until a sunrise
        // atomically adopts the new config. A new 240k day cannot enter a 24k model.
        boolean adoptCycle = clock == null || (clock.cycleTicks() != cycle && Math.floorMod(current, 24_000) == 0);
        int activeCycle = adoptCycle ? cycle : clock.cycleTicks();
        int candidate = SeasonCurve.dayTicks(activeCycle, SeasonAdapter.annualPhase(level),
                SeasonalDaylightConfig.EQUINOX_DAY_FRACTION.get(),
                SeasonalDaylightConfig.ANNUAL_AMPLITUDE.get());
        if (adoptCycle) clock = new ClockAccumulator(current, activeCycle, candidate);
        long next = clock.advance(current, true, candidate);
        saved.setClock(clock);
        if (level.getGameTime() % 20 == 0)
            DaylightNetwork.broadcast(level, clock.snapshot());
        return next;
    }
}
