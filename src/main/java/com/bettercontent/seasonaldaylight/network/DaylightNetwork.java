package com.bettercontent.seasonaldaylight.network;

import com.bettercontent.seasonaldaylight.client.ClientClock;
import com.bettercontent.seasonaldaylight.clock.ClockAccumulator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

/** Server-authoritative sky phase for local client extrapolation, not a gameTime packet. */
public final class DaylightNetwork {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("better_seasonal_daylight", "clock"), () -> PROTOCOL,
            PROTOCOL::equals, PROTOCOL::equals);
    private static boolean registered;

    private DaylightNetwork() {}

    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.registerMessage(0, PhasePacket.class, PhasePacket::encode, PhasePacket::decode,
                DaylightNetwork::receive);
    }

    public static void broadcast(ServerLevel level, ClockAccumulator.State state) {
        CHANNEL.send(PacketDistributor.DIMENSION.with(level::dimension), new PhasePacket(state));
    }

    public static void send(ServerPlayer player, ClockAccumulator.State state) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PhasePacket(state));
    }

    private static void receive(PhasePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientClock.accept(packet.state())));
        context.setPacketHandled(true);
    }

    public record PhasePacket(ClockAccumulator.State state) {
        private static void encode(PhasePacket packet, FriendlyByteBuf buf) {
            ClockAccumulator.State state = packet.state();
            buf.writeLong(state.lastOutput());
            buf.writeVarInt(state.cycleTicks());
            buf.writeVarInt(state.dayTicks());
            buf.writeVarInt(state.elapsedPhaseTicks());
            buf.writeBoolean(state.dayPhase());
        }
        private static PhasePacket decode(FriendlyByteBuf buf) {
            return new PhasePacket(new ClockAccumulator.State(buf.readLong(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readBoolean()));
        }
    }
}
