# Solstice modernization checklist

**Status:** Active audit and modernization tracker  
**Created:** 2026-11-26  
**Working branch:** `docs/modernize-ai-ci-meridian-status`  
**Tracking PR:** [#14 — Modernize AI governance, Meridian status, and Gradle CI](https://github.com/INFORMATION89/Solstice/pull/14)

This checklist defines the scope and acceptance criteria for modernizing Solstice's documentation, cross-repository coordination, AI-assisted workflow, and CI/Gradle pipeline. It is a tracking document, not a new authority for product decisions. `docs/PLAN.md` remains authoritative for Solstice decisions; `docs/RULES.md` remains authoritative for Solstice-specific engineering constraints; root `AGENTS.md` defines agent operations; `docs/AI_COST_POLICY.md` governs AI/tool/CI efficiency.

A checked item means that the stated evidence was inspected or the change was verified. A proposed change, green compile, or second-model opinion alone does not count as completion.

## 0. Baseline and current PR

- [x] Create the modernization branch and tracking PR #14 as a draft.
- [x] Add root `AGENTS.md` with the reading order, repository boundaries, task ownership, and evidence-based reporting.
- [x] Add Solstice-specific `docs/AI_COST_POLICY.md`, aligned with the shared Motion-UI AI policy and Meridian's cost policy.
- [x] Link the local policy from `docs/RULES.md`; update the entry points in `docs/PLAN.md` and `docs/CONTINUE.md`.
- [x] Compare Meridian-Engine's current master plan with Solstice's historical Meridian notes.
- [x] Record that Meridian remains at the MER-0.1 evidence/owner-review gate; do not describe the production engine, stack, API, or integration as complete.
- [x] Enable Gradle's local build cache in `gradle.properties`.
- [x] Consolidate formatting, Motion unit tests, Debug assembly, and optional main-branch Profile assembly into a single Gradle invocation/task graph.
- [x] Add workflow concurrency cancellation so newer runs for the same PR/branch supersede obsolete runs.
- [x] Consolidate debug and main-branch Profile builds into one Gradle workspace; split publication into a least-privilege job.
- [x] Scope signing secrets to main-only steps rather than the general build job, which also runs for pull requests.
- [x] Restrict temporary signing-file permissions with `umask 077`.
- [x] Pin artifact actions to verified immutable commit SHAs: upload-artifact v7.0.2 and download-artifact v8.0.2; both are official signed releases.
- [x] Verify PR head `e5a3f96784c5fbb403851014d968629d3afeb785` on run #260 (`45781291703`): checkout (including the canonical Motion submodule URL), JDK 21, Gradle setup, one combined verification/build step, and debug artifact upload all passed. The run produced artifact `solstice-basic-debug` (artifact ID `15097649394`).
- [ ] Verify the main-branch Profile APK is produced, downloaded, signed, named, and published as the expected testing pre-release.
- [x] Verify PR run #260 skipped main-only signing-secret steps; build job had only `contents: read`, and the separate `contents: write` publish job was skipped.
- [x] GitHub Actions parsed and executed the final workflow; Debug artifact path uploaded five files successfully. Profile artifact path and release job still require a protected-main run with approved signing secrets.
- [x] Timing sample is mixed, not a stable saving: baseline runs #237 and #239 each took 224 s; run #250 took 192 s (32 s / 14.3% lower), but the later exact-head run #260 took 254 s (30 s / 13.4% slower than baseline) despite successful Gradle-cache restoration. Run #260's combined Gradle task took 216 s and reported `BUILD SUCCESSFUL in 3m 36s`; 204 actionable tasks (184 executed, 18 from cache, 2 up-to-date). Both candidate runs restored Gradle cache. Do not claim a stable CI time or runner-cost saving from this sample.
- [x] Report the measured difference as variable: same configuration and cache restore do not guarantee faster hosted-runner wall time. Continue measuring several warm/cold runs and inspect job/task breakdowns; no invoice-level saving can be inferred.
- [ ] Obtain focused independent review of the workflow/security diff. **Opus review recommended** because CI credentials and release permissions are involved; this does not block implementation or replace tests/owner authorization.

## 1. Documentation inventory and authority

### Entry points and governance

- [x] Review root `README.md` identity, attribution, licence, test APK instructions and channel links; clarify that publication requires successful CI and configured signing secrets.
- [x] Replace the inherited `CONTRIBUTING.md` with Solstice-specific contribution, approval, AI, licence, and PR guidance.
- [x] Replace the inherited `INSTALL.md` with the current Solstice testing-release installation and Obtainium instructions; remove Breezy releases/F-Droid channels.
- [x] Replace the inherited `3RD_PARTY.md` with code-verified Solstice broadcast and ContentProvider integration documentation; label third-party compatibility as unverified where appropriate.
- [x] Replace inherited `HELP.md` with Solstice-specific support guidance for locations, sources, network/weather errors, background updates, local app-to-app sharing, and privacy caveats; remove Breezy-only launcher, release, forum, and unsupported-source instructions.
- [x] Review root `AGENTS.md` against actual repo layout, current PR workflow, mandatory reading order, and context-efficiency requirements.
- [x] Review the top-level authority links and current Meridian integration gate in `docs/PLAN.md`; deeper product-decision cleanup remains open.
- [ ] Review `docs/RULES.md` for contradictions, obsolete paths, policies repeated elsewhere, and approval boundaries that agents cannot follow consistently.
- [ ] Review `docs/AI_COST_POLICY.md` against the shared Motion-UI policy and Meridian policy; keep Solstice-specific Android/Gradle rules local and avoid copying volatile provider prices or quotas.
- [x] Replace the obsolete 2026-11-25 Motion PR inventory in `docs/CONTINUE.md` with the verified 2026-11-26 Motion/Meridian status and current PR #14 handoff. The older scheduled-session notes still need a broader age/relevance review.
- [ ] Review `docs/ROADMAP.md`: classify shipped, in-progress, proposed, blocked, deferred, and obsolete work accurately.
- [ ] Review `docs/LOG.md` for chronology and factual references; preserve historical entries, correct only demonstrably false current pointers through dated clarifications.
- [ ] Confirm the authority map and links among PLAN, RULES, ROADMAP, CONTINUE, LOG, AGENTS, and AI_COST_POLICY.
- [ ] Remove duplicated instructions where a concise authoritative link is sufficient.
- [ ] Ensure every document has a clear status (current, proposal, research, historical/archive, or superseded) where ambiguity could cause an agent to implement it accidentally.

### Product, design, and writing

- [ ] Review `docs/DESIGN.md` against the decisions currently recorded in PLAN.
- [ ] Review `docs/MOTION.md` against the actual Motion APIs/tokens and current Solstice call sites; identify legacy spring/value tables.
- [ ] Review `docs/UI_SYSTEMS.md` inventory against current source code; refresh counts, paths, dependency claims, and the statement about what has or has not been built.
- [ ] Review `docs/WRITING.md` for approved versus proposed copy, English/Mexican Spanish consistency, and weather terminology.
- [ ] Review `docs/PROPOSALS.md` and move only explicitly approved decisions into PLAN; keep unapproved proposals clearly non-implementable.
- [ ] Verify that documentation continues to prioritize feels-like temperature while keeping actual air temperature visible, and does not present a derived metric as a personal measurement.
- [ ] Check accessibility, reduced motion, RTL, loading/empty/error/offline states, and foldable/tablet layout rules remain consistent across design documents.

### Weather sources and Meridian references

- [ ] Review `docs/SOURCES.md` and distinguish existing source behavior, accepted decisions, proposed providers, and future Meridian responsibilities.
- [x] Initial review of `docs/ENGINE_SPEC.md` and `docs/MERIDIAN_PLAN.md` identified stale claims (repository name, KMP, Room, API/Flow, distribution and milestone assumptions); a deeper meteorological content reconciliation remains pending in Meridian's own gates.
- [x] Mark `docs/ENGINE_SPEC.md` and `docs/MERIDIAN_PLAN.md` as historical Solstice-side research references and point to Meridian-Engine's master plan as the architecture/status authority.
- [ ] Do not silently copy Meridian's current architecture into Solstice or allow Solstice docs to become a second source of truth.
- [ ] Verify Meridian milestone status against its current `PLAN.md`, exact branch/PR heads, and CI before updating claims.
- [ ] Keep the Solstice integration gate explicit: no production dependency/API/storage/distribution decision until Meridian's relevant evidence and owner gates are approved.
- [ ] Ensure no old instruction asks an agent to recreate the Meridian repository, repeat its M0 bootstrap, or start production weather features before the current gates.
- [ ] Review references to Open-Meteo, providers, radar, observations, model spread, alert feeds, pricing, licences, and availability; revalidate time-sensitive facts at the implementation milestone.

### Motion and framework integration

- [x] Refresh `docs/MOTION_ADOPTION_PLAN.md` against the current Motion-UI main SHA, Solstice's pinned submodule SHA, merged PR #4, open PR #7, and both relevant CI results.
- [x] Correct the submodule URL to canonical `INFORMATION89/Motion-UI.git` while deliberately preserving the existing submodule commit.
- [x] Verify the canonical URL checkout, current `motion/` submodule commit, and recursive submodule checkout on the exact current PR head: run #260 checkout passed; the combined Gradle task also passed.
- [x] Distinguish Motion Core MOT0.1 merged in Motion PR #4 from the standalone Motion Lab PR #7 still in Draft, and the planned but non-existent `:motion-ui` module.
- [ ] Do not describe a planned UI kit, iOS, desktop, or cross-platform capability as currently available in Solstice.
- [ ] Verify ownership boundaries: generic motion/framework behavior belongs in Motion; weather behavior, screens, identity, navigation policy, and Solstice configuration remain here.

## 2. Privacy, security, licensing, and public claims

- [x] Create `docs/PRIVACY_DATA_MAP.md` as an internal code-backed inventory of location, geocoding, weather providers, GitHub update checks, local app-to-app sharing, and unresolved privacy evidence.
- [ ] Rewrite and validate root `PRIVACY.md`: it currently contains Breezy Weather wording and cannot be treated as Solstice's finished privacy notice. Use `docs/PRIVACY_DATA_MAP.md` as an evidence starting point, not as a legal notice.
- [ ] Update the custom `READ_PROVIDER` permission label/description strings: they still say “Breezy Weather” and mention exported location coordinates; keep this as a separate user-visible copy change requiring the approval in RULES.md.
- [ ] Inventory actual permissions, weather/location/geocoding providers, update checker, crash/error reporting, data sharing, background work, and app-to-app sharing against implementation.
- [ ] Confirm telemetry and data contribution remain off by default; onboarding invitation is optional, informed, separate from core functionality, and revocable.
- [ ] Ensure each proposed data flow states what is collected, purpose, destination/recipients, retention, controls, and an inspectable example payload before any collection is implemented.
- [ ] Check the current app's third-party provider privacy links and attribution against actual enabled providers, not inherited upstream defaults alone.
- [ ] Review app name/package identifier, upstream notices, LGPL-3.0 obligations, modifications, bundled dependencies, and third-party licence notices.
- [ ] Verify no upstream privacy statement or unsupported claim remains in Solstice-facing About, Settings, README, Play Store drafts, or website text.
- [ ] Review GitHub Actions permissions, secret exposure, signing, artifact provenance, release replacement behavior, dependency pinning, and untrusted PR execution.
- [ ] Keep signing credentials out of PR jobs, build logs, artifacts, cache entries, and broadly scoped job environments.
- [ ] Confirm no security gate is weakened for cost or speed.

## 3. CI, Gradle, and build reproducibility

- [ ] Inventory every active workflow under `.github/workflows/` and identify its triggers, jobs, permissions, secrets, artifacts, runner, and purpose.
- [ ] Verify the JDK version from `.github/.java-version`, Gradle Wrapper version/checksum, Android Gradle Plugin, Kotlin, SDK/build-tools, and action versions.
- [ ] Confirm Gradle caching has a clear owner (`gradle/actions/setup-gradle`) and avoid overlapping cache actions for the same state.
- [ ] Validate local build cache behavior after `org.gradle.caching=true`; confirm no stale-input or non-cacheable task problem.
- [ ] Measure whether `org.gradle.parallel`, configuration cache, configuration-on-demand, build cache, and task consolidation are safe and beneficial for this project's actual module/task graph before changing them.
- [ ] Remove redundant Gradle invocations or repeated environment setup only where task dependencies and artifact correctness are preserved.
- [ ] Confirm debug and Profile tasks can share the same Gradle user home/workspace without leaking secrets into the build job.
- [ ] Confirm release publication is a separate least-privilege job and has a reliable artifact handoff.
- [ ] Check whether tests can run concurrently with safe build tasks without increasing memory pressure or introducing nondeterminism.
- [ ] Evaluate path filters only if they cannot miss root Gradle, version catalog, submodule, resource, workflow, or shared source changes.
- [ ] Keep Android SDK/emulator-capable jobs on compatible runners. Do not replace them with a generic Gradle Docker image by default.
- [ ] Consider a custom container image only after proving it includes the required Android toolchain, is pinned by digest, is maintained, and improves measured end-to-end cost/time.
- [ ] Verify cache keys and restore behavior for Gradle wrapper distributions, dependencies, transforms, and build outputs; never cache secrets or signing material.
- [ ] Record cold and warm build measurements, cache hits, runner minutes, artifact size, and CI failure/retry rates where available.
- [ ] Ensure a failed required check blocks merge; rerun only failed jobs after inspecting the cause.
- [ ] Keep test APK publication limited to pushes to protected `main` and report missing secrets/build artifacts honestly.

## 4. AI assignment, task sizing, and cost controls

- [ ] Apply one primary agent per coherent issue/PR, with a clear scope, acceptance criteria, stop condition, and handoff.
- [ ] Assign routine extraction, formatting, and small mechanical documentation edits to an efficient model.
- [ ] Prefer Claude Sonnet or ChatGPT/Codex for normal implementation, Android/Compose work, tests, and scoped debugging.
- [ ] Use ChatGPT/Codex as coordinator when cross-repository context, GitHub operations, architecture tracking, or multi-step orchestration adds value.
- [ ] Mark **Opus review recommended** for complex cross-module architecture, difficult root-cause work, concurrency/animation, security/privacy/licensing, data migrations, high-risk CI/release, or major refactors when an independent pass can reduce risk.
- [ ] Do not block the first implementation on Opus; do not duplicate whole-repository exploration or full CI across agents.
- [ ] Do not use paid APIs, credits, overages, subscriptions, hosted runners, or extra tiers without explicit approval.
- [ ] Report actual model/tool usage only when available; never infer token, dollar, or subscription consumption from message counts.
- [ ] Include exact files, SHA, checks, untested areas, remaining risks, and next action in every non-trivial handoff.

## 5. Verification and closure criteria

- [x] GitHub Actions parsed and executed the updated YAML successfully on the exact PR head.
- [ ] Validate all changed Markdown syntax and all internal/external documentation links.
- [ ] Check internal relative links and high-value external links.
- [ ] Search for obsolete repository names, paths, PR numbers, branch names, dates, model names, provider terms, and old architecture choices.
- [ ] Compare every factual status claim against the relevant repository's current main/branch/PR/CI evidence.
- [ ] Review the final diff for contradictory, duplicated, speculative, or stale instructions.
- [ ] Run documentation-only validation without wasting a full Android matrix.
- [x] Run the appropriate workflow checks for build/Gradle/CI changes: final exact-head run #250 completed successfully.
- [ ] Inspect exact PR head SHA and CI run SHA before accepting green checks.
- [ ] Mark Pixel/physical-device checks NOT TESTED or DEFERRED unless actually performed.
- [ ] Obtain independent Opus review of CI credentials/release permissions. **Opus review recommended**; not performed in this pass and no paid usage authorized.
- [ ] Update PLAN only for changed decisions, LOG for dated history, ROADMAP for task status, and CONTINUE for the next actionable handoff.
- [ ] Close the modernization only when the applicable checklist sections are complete, unresolved items are explicitly deferred with reasons, and the owner has reviewed any strategic/privacy/paid-cost decisions.

## 6. Suggested execution order and ownership

| Order | Work package | Primary owner | Second review | Dependency / exit condition |
|---|---|---|---|---|
| 0 | PR #14 security hardening and CI validation | ChatGPT/Codex | Opus recommended | Secrets isolated; required CI green; artifacts verified |
| 1 | Full documentation inventory and authority cleanup | ChatGPT/Codex coordinator; Sonnet implementation | Opus only for conflicting architecture/security claims | No stale active instructions or duplicated authority |
| 2 | Meridian/Motion cross-repository reconciliation | ChatGPT/Codex | Opus recommended for architecture boundary changes | Exact external status verified; no copied source of truth |
| 3 | Privacy, data-flow, attribution, and licensing audit | ChatGPT/Codex with source/code inspection | Opus strongly recommended | Every public claim matches implementation; unresolved legal/privacy points escalated |
| 4 | Gradle/CI performance and reproducibility measurements | ChatGPT/Codex | Sonnet focused review; Opus if security-sensitive design remains | Comparable before/after evidence; no weakened gate |
| 5 | Final docs validation, links, roadmap and handoff | Efficient model/Sonnet | ChatGPT/Codex final review | All modified docs consistent and PR ready |

## 7. Findings already confirmed during the initial pass

These are concrete audit leads, not a substitute for the full review:

- `PRIVACY.md` still opens with “Breezy Weather” and describes upstream behavior. Validate every statement against Solstice's current implementation before publishing a replacement.
- `.gitmodules` still points to `https://github.com/INFORMATION89/Motion.git`, while the canonical repository identified by the Motion adoption plan is `INFORMATION89/Motion-UI`. Verify the current Motion repository/commit and migrate the URL deliberately in a separate, tested change.
- `docs/ENGINE_SPEC.md` and `docs/MERIDIAN_PLAN.md` still present 2026-11-23 drafts with older assumptions such as KMP, Room, a planned repository name, and a proposed submodule/composite-build integration. They must not override Meridian-Engine's current master plan.
- `docs/MOTION_ADOPTION_PLAN.md` has been refreshed: Motion PR #4 is merged, PR #7 remains Draft with a Spotless failure, the submodule is still pinned to the pre-MOT0.1 base commit, and `:motion-ui` is not present.
- `docs/UI_SYSTEMS.md` identifies itself as a 2026-11-19 research snapshot and says listed work is not built; refresh its inventory against current code before using it to assign implementation work.
- The current Gradle Wrapper is 9.9.1, JDK is pinned to 21, `gradle/actions/setup-gradle` is already in use, and local Gradle build caching was disabled before this modernization branch. These facts should be rechecked on the final PR head.
- The current workflow groups Spotless, Motion unit tests, Debug assembly, and optional main-only Profile assembly in one Gradle invocation, and cancels obsolete runs for the same PR/branch. This is a design-level optimization; measured savings remain unknown.
- The CI refactor scopes signing-secret access to main-only steps, pins artifact actions, combines verification and variant assembly in one Gradle invocation, and cancels obsolete runs. Exact-head run #260 passed, uploaded the Debug APK, and correctly skipped Profile signing/publication on the PR. Performance measurements are variable: #250 was 192 s, #260 was 254 s versus 224 s baseline runs; no stable saving is demonstrated.

## 8. Explicit non-goals

- Do not redesign Solstice's UI, add product features, or change weather behavior as part of this documentation/CI audit.
- Do not select Meridian's production language, storage, public API, providers, or packaging on Meridian's behalf.
- Do not claim iOS/desktop support for Solstice because Meridian or Motion may target additional platforms.
- Do not create a custom Gradle container image unless measurements justify the maintenance and supply-chain cost.
- Do not merge PR #14, change protected branch settings, publish a production release, or incur paid AI/API/runner usage without the required authorization.

### Privacy and data-flow inspection notes (initial code review, 2026-11-26)

Detailed code-backed findings and unresolved questions are maintained in [PRIVACY_DATA_MAP.md](PRIVACY_DATA_MAP.md). It is an internal technical audit only, not a public notice or legal approval.

The following are code-backed findings from the initial review. They define what the full privacy audit must explain; they are not yet a complete legal/privacy approval.

- The manifest requests fine/coarse location, background location, internet/network state, notifications, foreground-service, boot, and widget-related permissions. The background-location disclosure should explain that it supports refresh of the latest Android-known current position.
- The default location source falls back to Android native location. IP.SB is another available location source and calls `https://api.ip.sb/geoip`; using it necessarily exposes the requesting network's public IP to IP.SB, which estimates location from that IP. It does not require Android location permission.
- Location search includes GeoNames; reverse geocoding uses Android's Geocoder first where available and Nominatim as a remote option/fallback. Weather source selection is per feature/place; currently registered sources include Open-Meteo, FPAS, MET Norway, NCEI, OpenWeather, Pirate Weather, WMO severe weather, ECCC, and NWS. AccuWeather is instantiated in code but Solstice's SourceManager comments say it is disabled from the active worldwide/national source lists for now. Confirm the actual UI-selectable set and exact endpoint fields before drafting final wording.
- The app-update preference `isAppUpdateCheckEnabled` defaults to `false`. When enabled, the GitHub latest-release request is throttled to at most once per day unless forced; the current app version is compared locally with the returned release version. Do not describe this as telemetry or claim the version is transmitted without separate evidence.
- Solstice exports a read-only weather ContentProvider, protected by its custom dangerous `READ_PROVIDER` permission. The provider returns saved-location data including latitude/longitude and weather results to apps that receive that permission. The permission prompt strings still say “Breezy Weather” and require Solstice wording.
- Settings exposes a per-broadcast-source package selector. Gadgetbridge export includes place name, latitude/longitude, current conditions, forecast, and air-quality data. The separate update notifier broadcasts location IDs to configured recipient packages. These are local app-to-app disclosures, not proof of server-side telemetry.
- Crash log export inspected so far creates a local logcat file for the user to open/share. No automatic crash-upload service was observed in the inspected code; complete dependency/workflow review is still required before making an absolute “no diagnostics are transmitted” claim.
- The privacy notice currently describes Breezy Weather directly, so it is not a suitable Solstice notice. Replace it only after the complete provider, permission, local-sharing, retention, and build-flavor inventory is reconciled.
