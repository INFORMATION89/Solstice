# Meridian M0: the empty repo (Saturday step 10, only if time is left)

Status: **checklist** (2026-11-24). No engine code; M1 starts in a later session from its own spec.

## Create

- [ ] GitHub repo `INFORMATION89/Meridian`, public, Apache 2.0 (GitHub's licence picker), default branch
  `main`.
- [ ] Clone it beside Solstice, not inside (it becomes a submodule only when M1 has something to use).

## Files

- [ ] `LICENSE`: Apache 2.0 full text. `NOTICE`: "Meridian, Copyright 2026 INFORMATION89".
- [ ] `README.md`: one paragraph (a weather engine as a library: several models, local corrections,
  official alerts, with provenance), status "early, nothing usable yet", link to the specs.
- [ ] `CLEAN_ROOM.md`: a copy of Solstice's RULES.md section 5 (allowed sources, spec then build, each
  PR lists what was consulted).
- [ ] `docs/`: copies of ENGINE_SPEC.md, MERIDIAN_PLAN.md and `specs/M1`–`M7` (Meridian's own home from
  now on; Solstice keeps a link).
- [ ] `.gitignore` for Gradle, IDE files, `local.properties`, keys.
- [ ] `.github/workflows/ci.yml`: on push and pull request, Ubuntu, Java 21, `./gradlew build`.
- [ ] SPDX header `Apache-2.0` on every source file (none yet; CI check comes with M1).

## Gradle

- [ ] Gradle wrapper (latest stable, checked on Saturday); commit `gradlew` and the wrapper jar, never
  `gradlew.bat` (same rule as Solstice).
- [ ] `gradle/libs.versions.toml`: Kotlin, Kotlin Multiplatform, Ktor, kotlinx.serialization,
  kotlinx-datetime, xmlutil, Room, DataStore. Versions picked on Saturday from each project's latest
  stable release (MERIDIAN_PLAN.md section 2 gives the minimums: kotlinx-datetime 0.7+, Room 2.7+,
  xmlutil 1.0.x); none typed into build files.
- [ ] `settings.gradle.kts` with the modules from MERIDIAN_PLAN.md section 3: `core`,
  `sources:openmeteo`, `sources:aviationweather`, `sources:wmo`, `sources:fpas`, `storage-room`,
  `testing`, `cli`.
- [ ] Each module: a `build.gradle.kts` with targets JVM and Android (macOS comes with M8), no sources.
  `cli` is JVM only.
- [ ] A shared convention for the Kotlin settings (one place, not repeated per module).

## Check

- [ ] `./gradlew build` passes locally and on CI.
- [ ] Branch protection on `main`: PRs only, CI must pass (after the first green run).
- [ ] CONTINUE.md and LOG.md in Solstice updated with the repo link.

## Done when

CI green on an empty build (MERIDIAN_PLAN.md section 9).
