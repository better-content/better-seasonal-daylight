package com.bettercontent.seasonaldaylight.client;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Prevent tiny normal network-latency sky rewinds without masking /time commands. */
@Mixin(ClientPacketListener.class)
public abstract class ClientTimePacketMixin {
    @ModifyArg(method = "handleSetTime", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;setDayTime(J)V"), index = 0)
    private long betterSeasonalDaylight$packetSky(long packetDayTime, ClientboundSetTimePacket packet) {
        ClientPacketListener listener = (ClientPacketListener) (Object) this;
        return ClientClock.vanillaPacketTime(listener.getLevel(), packetDayTime, packet.getGameTime());
    }
}
