package com.bettercontent.seasonaldaylight.mixin.client;

import com.bettercontent.seasonaldaylight.client.ClientClock;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Client extrapolates the same bounded phase schedule, not vanilla +1 every tick. */
@Mixin(ClientLevel.class)
public abstract class ClientDayTimeMixin {
    @Unique private long betterSeasonalDaylight$beforeTick;

    @Inject(method = "tickTime", at = @At("HEAD"))
    private void betterSeasonalDaylight$recordTime(CallbackInfo ci) {
        betterSeasonalDaylight$beforeTick = ((ClientLevel) (Object) this).getDayTime();
    }

    @Inject(method = "tickTime", at = @At("TAIL"))
    private void betterSeasonalDaylight$advanceTime(CallbackInfo ci) {
        ClientLevel level = (ClientLevel) (Object) this;
        long vanillaNext = level.getDayTime();
        if (vanillaNext != betterSeasonalDaylight$beforeTick + 1) return;
        long adjusted = ClientClock.adjust(level, betterSeasonalDaylight$beforeTick, vanillaNext);
        if (adjusted != vanillaNext) level.setDayTime(adjusted);
    }
}
