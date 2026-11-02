package com.bettercontent.seasonaldaylight;

import com.bettercontent.seasonaldaylight.runtime.SeasonalDaylightRuntime;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(BetterSeasonalDaylight.MOD_ID)
public final class BetterSeasonalDaylight {
    public static final String MOD_ID = "better_seasonal_daylight";
    private static final String OLD_CLOCK_ID = "simpledaylengthextender";

    public BetterSeasonalDaylight() {
        if (ModList.get().isLoaded(OLD_CLOCK_ID)) {
            throw new IllegalStateException("Better Seasonal Daylight cannot run with Simple Day Length Extender: "
                    + "remove simpledaylengthextender together with activating this mod. "
                    + "Two daylight clock owners must never be installed at once.");
        }
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SeasonalDaylightConfig.SPEC);
        SeasonalDaylightRuntime.register();
    }
}
