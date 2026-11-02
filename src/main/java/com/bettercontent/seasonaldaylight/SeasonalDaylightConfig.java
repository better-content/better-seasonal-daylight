package com.bettercontent.seasonaldaylight;

import net.minecraftforge.common.ForgeConfigSpec;

/** Server-authoritative policy; changes to cycle settings take effect only at safe day boundaries. */
public final class SeasonalDaylightConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.LongValue CYCLE_TICKS;
    public static final ForgeConfigSpec.DoubleValue EQUINOX_DAY_FRACTION;
    public static final ForgeConfigSpec.DoubleValue ANNUAL_AMPLITUDE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Seasonal sky-clock settings; never changes gameTime, TPS, or machine simulation.");
        CYCLE_TICKS = builder.comment("Nominal server ticks per complete sunrise-to-sunrise cycle (50000 = 41m40s at 20 TPS).")
                .defineInRange("cycleTicks", 50_000L, 24_000L, 240_000L);
        EQUINOX_DAY_FRACTION = builder.comment("Fraction of the cycle spent in daytime at both equinoxes.")
                .defineInRange("equinoxDayFraction", 0.5D, 0.3D, 0.7D);
        ANNUAL_AMPLITUDE = builder.comment("Annual swing around the equinox fraction; default yields summer 70/30 and winter 30/70.")
                .defineInRange("annualAmplitude", 0.2D, 0.0D, 0.2D);
        SPEC = builder.build();
    }

    private SeasonalDaylightConfig() {}
}
