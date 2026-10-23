# SkyPeep 0.3.32 — safe saved-data startup and mid-flight records

## Source-confirmed issues

The game previously invoked LibGDX `Preferences.getInteger/getBoolean` directly
throughout bootstrap. A single incorrectly typed stored value may throw at read
time and prevent all valid progress from loading. High scores were recalculated
at gate passages, but persisted only by a later pause/result/full-save, so an
unexpected app termination mid-flight could discard a new personal best.

## Implementation

- All bootstrap primitive reads use `SkyPeepPreferenceRead`. A malformed entry
  returns only its individual fallback and logs its key, without wiping or
  hiding other healthy keys. Existing range/ownership checks remain.
- On each scored gate, `score` is saturating and new global/per-difficulty bests
  flush immediately with the existing two preference keys and no extra data.
  Non-record passages do not add storage writes. Existing results still save
  the full session state.
- Android share chooser handles common activity/security errors rather than
  allowing an exception to terminate the activity.
- Added JVM regressions for malformed integer/boolean reads and for the exact
  score-persistence key set and invalid-route safety.

## Limits / QA gates

- Malformed entries are replaced by defaults only in memory. A subsequent
  full save may replace those damaged values; there is no claim of recovery
  of bytes that were already corrupted before startup.
- The new record is only durable if the underlying preference flush succeeds;
  the existing storage error handler logs failures and retains RAM state.
- CI Gradle tests, Bash syntax, dead-code audit, source-art hash, offline
  Android merged manifest, build artifacts and emulator navigation must pass.
- Compare existing four UI reference illustrations and all six worlds. No
  existing visual assets, collision thresholds or progression goals changed.
- User-device crash traces and GPU context-loss behaviour remain to be verified
  on actual hardware; emulator success cannot prove zero crashes.
