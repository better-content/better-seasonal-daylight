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
        EQUINOX_DAY_FRACTION = builder.comment("Fraction of the cycle spent in daytime at both equinoxes. Every season is bounded to at least 5/9 day.")
                .defineInRange("equinoxDayFraction", 0.65D, 0.3D, 0.7D);
        ANNUAL_AMPLITUDE = builder.comment("Annual swing around the equinox fraction. Default varies smoothly from 5/9 winter daytime to about 74.4% summer daytime.")
                .defineInRange("annualAmplitude", 0.65D - 5.0D / 9.0D, 0.0D, 0.2D);
        SPEC = builder.build();
    }

    private SeasonalDaylightConfig() {}
}
