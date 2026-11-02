package com.bettercontent.seasonaldaylight.mixin;

import com.bettercontent.seasonaldaylight.runtime.SeasonalDaylightRuntime;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Replace only the sky-clock write; vanilla gameTime and scheduled events still run. */
@Mixin(ServerLevel.class)
public abstract class ServerDayTimeMixin {
    @ModifyArg(method = "tickTime", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V"), index = 0)
    private long betterSeasonalDaylight$skyOnly(long vanillaNext) {
        return SeasonalDaylightRuntime.adjust((ServerLevel) (Object) this, vanillaNext);
    }
}
