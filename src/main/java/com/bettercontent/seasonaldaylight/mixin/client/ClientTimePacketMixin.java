package com.bettercontent.seasonaldaylight.mixin.client;

import com.bettercontent.seasonaldaylight.client.ClientClock;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Prevent tiny normal network-latency sky rewinds without masking /time commands. */
@Mixin(ClientPacketListener.class)
public abstract class ClientTimePacketMixin {
    @Redirect(method = "handleSetTime", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;setDayTime(J)V"))
    private void betterSeasonalDaylight$packetSky(ClientLevel level, long packetDayTime, ClientboundSetTimePacket packet) {
        // Redirect can capture the enclosing packet argument; ModifyArg cannot.
        level.setDayTime(ClientClock.vanillaPacketTime(level, packetDayTime, packet.getGameTime()));
    }
}
