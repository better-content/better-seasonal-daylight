# Better Seasonal Daylight

Forge 1.20.1 / 47.4.22, Java 17, Serene Seasons 9.1.0.2. This standalone mod replaces Simple Day Length Extender, never runs alongside it. The 2026-11-02 source-only batch did not deploy it or verify gameplay, loader, client/server or world behavior.

## Current consolidation scope (2026-11-03)

The later Better Content consolidation authorization selects a fresh rebuild and atomic fresh-world experimental pack activation in place of the old clock owner. Local candidate commits, existing source/unit checks and exact-artifact pack Smoke are authorized under coordinator control; gate-controlled source publication requires every applicable gate. This does not waive a publication check, qualify runtime behavior or permit upload of restricted pack binaries. The prior implementation-only/no-commit/no-push limits below describe the earlier batch. Existing worlds, saved calendars and rollback configuration remain protected; no old-world repair/reset, owner-native seasonal-cycle campaign or visual acceptance is claimed.

## Intended sky clock

A full sunrise-to-sunrise Overworld cycle remains **50,000 simulation ticks** (41m40s at 20 TPS). Its daylight share changes smoothly over Serene Seasons' actual northern annual phase: equinox 50% (20m50s day / 20m50s night), summer solstice 70% (29m10s / 12m30s), winter solstice 30% (12m30s / 29m10s). The opposite half adjusts so total cycle duration stays fixed. Day/night durations include twilight by game-clock phase, not astronomical observation. This is a global sky clock, not a biome/latitude simulation. Server-authoritative sky/dayTime only: gameTime, TPS, entities, machines and ordinary simulation are unchanged. Serene Seasons `day_duration` stays at 24,000 logical dayTime ticks; do **not** change it to 50,000. Seasonal trees and the calendar use the same Serene Seasons timeline.

`config/better_seasonal_daylight-common.toml` exposes cycleTicks, equinoxDayFraction and annualAmplitude. **Known limitation:** a reliable brand-new-world marker is unavailable for an existing pack world without this mod's prior SavedData; even a world at gameTime/dayTime zero might be an existing world. Consequently there is no first-player auto-pause: it could unexpectedly freeze an existing world. The clock continues on empty servers and respects deliberate `doDaylightCycle=false`; Serene Seasons' independent `progress_when_offline` setting still governs its calendar. Fixed-time and non-Overworld dimensions are not targeted. Serene Seasons is a required dependency (fail visibly if absent). An installed `simpledaylengthextender` is explicitly rejected instead of stacking two clocks.

## Historical source-only replacement plan

The earlier source batch deferred installation. The later consolidation selects atomic replacement in the isolated candidate: remove the Simple Day Length Extender manifest from that candidate and activate the Better Seasonal Daylight artifact/manifest in the same change. Preserve the existing `config/simpledaylengthextender-common.toml` (day×3, night×1, boundary 13000) as rollback data unless explicitly directed otherwise; never activate both clocks. Do not edit existing world time, Serene Seasons season state, SS `day_duration`, or player saves. Fresh artifact identity and exact-artifact Smoke remain required; neither establishes native seasonal-cycle qualification.

## Historical source-batch checks

The earlier batch used the cached ForgeGradle toolchain, offline, max two workers, and explicit compile/assembly plus isolated JUnit tests under `/home/dev/.tmp/better-content-gradle.lock`. `./gradlew --offline --no-daemon --max-workers=2 compileJava test` is a source/unit check, **not** gameplay or loader verification. The later consolidation adds only the authorized exact-artifact pack Smoke; owner-native, seasonal-cycle and visual campaigns remain outside its scope.
