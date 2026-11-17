package com.bettercontent.seasonaldaylight.mixin;

import com.bettercontent.seasonaldaylight.runtime.SeasonalDaylightRuntime;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adjust the sky clock after vanilla's tick, without depending on a transformed call signature. */
@Mixin(ServerLevel.class)
public abstract class ServerDayTimeMixin {
    @Unique private long betterSeasonalDaylight$beforeTick;

    @Inject(method = "tickTime", at = @At("HEAD"))
    private void betterSeasonalDaylight$recordTime(CallbackInfo ci) {
        betterSeasonalDaylight$beforeTick = ((ServerLevel) (Object) this).getDayTime();
    }

    @Inject(method = "tickTime", at = @At("TAIL"))
    private void betterSeasonalDaylight$advanceTime(CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        long vanillaNext = level.getDayTime();
        if (vanillaNext != betterSeasonalDaylight$beforeTick + 1) return;
        long adjusted = SeasonalDaylightRuntime.adjust(level, betterSeasonalDaylight$beforeTick, vanillaNext);
        if (adjusted != vanillaNext) level.setDayTime(adjusted);
    }
}
