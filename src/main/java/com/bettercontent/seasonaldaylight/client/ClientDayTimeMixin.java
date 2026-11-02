package com.bettercontent.seasonaldaylight.client;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Client extrapolates the same bounded phase schedule, not vanilla +1 every tick. */
@Mixin(ClientLevel.class)
public abstract class ClientDayTimeMixin {
    @ModifyArg(method = "tickTime", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;setDayTime(J)V"), index = 0)
    private long betterSeasonalDaylight$skyOnly(long vanillaNext) {
        return ClientClock.adjust((ClientLevel) (Object) this, vanillaNext);
    }
}
