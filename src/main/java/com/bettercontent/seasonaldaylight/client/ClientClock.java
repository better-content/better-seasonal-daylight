package com.bettercontent.seasonaldaylight.client;

import com.bettercontent.seasonaldaylight.clock.ClockAccumulator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

/** Local sky interpolation between authoritative phase packets; never changes gameTime. */
public final class ClientClock {
    private static ClientLevel currentLevel;
    private static ClockAccumulator clock;
    private static long lastVanillaPacketGameTime = Long.MIN_VALUE;
    private static long lastVanillaPacketDayTime = Long.MIN_VALUE;

    private ClientClock() {}

    public static void accept(ClockAccumulator.State state) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level.dimension() != Level.OVERWORLD) return;
        forLevel(level);
        // An older in-flight phase packet must not turn a deliberate server-side
        // doDaylightCycle=false back on (ClientLevel.setDayTime(positive) does that).
        if (!level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) return;
        ClockAccumulator authoritative;
        try { authoritative = ClockAccumulator.restore(state); }
        catch (IllegalArgumentException invalid) { return; }
        long localTime = level.getDayTime();
        long serverTime = state.lastOutput();
        if (localTime < serverTime || localTime - serverTime > 8) {
            // Forward sync, or a deliberate large /time-back command: server owns it.
            level.setDayTime(serverTime);
            clock = authoritative;
        } else if (localTime == serverTime) {
            clock = authoritative;
        } else if (clock == null || clock.cycleTicks() != state.cycleTicks()
                || clock.dayTicks() != state.dayTicks()) {
            // A packet delayed by a few local ticks must not move the sun backward.
            // Rebase once to its new phase length; later packets restore exact state.
            clock = new ClockAccumulator(localTime, state.cycleTicks(), state.dayTicks());
        }
    }

    public static long adjust(ClientLevel level, long vanillaNext) {
        if (level.dimension() != Level.OVERWORLD) return vanillaNext;
        forLevel(level);
        if (clock == null || vanillaNext != level.getDayTime() + 1) return vanillaNext;
        return clock.advance(level.getDayTime(), true, clock.dayTicks());
    }

    /** Ignore only tiny *ordinary* vanilla packet rewinds; commands and large jumps pass. */
    public static long vanillaPacketTime(ClientLevel level, long packetDayTime, long packetGameTime) {
        if (level.dimension() != Level.OVERWORLD || packetDayTime < 0) return packetDayTime;
        forLevel(level);
        long current = level.getDayTime();
        boolean ordinary = lastVanillaPacketGameTime != Long.MIN_VALUE
                && packetGameTime - lastVanillaPacketGameTime >= 15
                && packetDayTime >= lastVanillaPacketDayTime;
        lastVanillaPacketGameTime = packetGameTime;
        lastVanillaPacketDayTime = packetDayTime;
        if (clock != null && ordinary && packetDayTime < current && current - packetDayTime <= 8)
            return current;
        return packetDayTime;
    }

    private static void forLevel(ClientLevel level) {
        if (level != currentLevel) {
            currentLevel = level;
            clock = null;
            lastVanillaPacketGameTime = Long.MIN_VALUE;
            lastVanillaPacketDayTime = Long.MIN_VALUE;
        }
    }
}
