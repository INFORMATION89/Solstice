# Solstice modernization checklist

**Scope:** Solstice Android application repository only  
**Origin:** [#14 — Solstice documentation, AI governance, and Gradle CI](https://github.com/INFORMATION89/Solstice/pull/14) (merged). This follow-up restores its baseline on current `main` and reconciles later changes.  
**Status snapshot:** 2026-11-27

This checklist covers Solstice-owned documentation, governance, privacy, build/CI, and release
readiness. It is not an implementation or status tracker for external dependencies. Product
decisions remain in [PLAN.md](PLAN.md); app engineering constraints in [RULES.md](RULES.md);
agent operations in root [AGENTS.md](../AGENTS.md); app tasks in [ROADMAP.md](ROADMAP.md).

External dependency information is limited to what Solstice consumes and what the app must continue
to own, in [INTEGRATIONS.md](INTEGRATIONS.md). Dependency roadmaps and internal milestones do not
belong in this checklist.

A checked item means its stated evidence was inspected or the change was verified. A proposal,
successful build, or unperformed specialist review does not count as completion.

## 1. Documentation ownership and consistency

- [x] Add root `AGENTS.md` with the mandatory reading order, Solstice project boundaries, work
  method, AI task ownership, and evidence-based reporting.
- [x] Add Solstice-specific `docs/AI_COST_POLICY.md` and link the shared AI-use policy.
- [x] Establish one authority per subject: PLAN, RULES, ROADMAP, LOG, CONTINUE, app design references,
  integration boundary, and privacy evidence map.
- [x] Keep PLAN and ROADMAP exclusively about the Solstice product, app code, and app work.
- [x] Keep CONTINUE limited to the current Solstice session and next app-owned task.
- [ ] Keep LOG chronological; historical entries are not current implementation instructions.
- [x] Add `docs/INTEGRATIONS.md` as the only consumer-side dependency boundary document.
- [x] Remove duplicate dependency roadmaps, lab plans, weather-engine specs, model-test plans, and
  internal milestone specs from the active Solstice documentation tree.
- [x] Search all active docs for broken links to removed files, stale instructions, and dependency
  implementation work that was accidentally copied into the app plan.
- [ ] Validate internal relative links and high-value external links after the final documentation
  diff.
- [x] Review the remaining app UX and research references for stale implementation claims.
- [ ] Confirm feels-like temperature remains a prominent useful metric while actual air temperature
  stays visible; don't describe feels-like as a personal measurement.
- [ ] Ensure app proposals are visibly marked as proposals and are not mistaken for approved work.

## 2. Privacy, security, licensing, and public claims

- [x] Create `docs/PRIVACY_DATA_MAP.md` as an internal code-backed inventory of location,
  geocoding, weather providers, update checks, local app-to-app sharing, and unresolved questions.
- [ ] Complete the code-path audit for background location, permissions, providers/endpoints,
  refresh scheduling, app-to-app data disclosures, diagnostics, retention/deletion, and build
  variants.
- [ ] Rewrite and validate root `PRIVACY.md`. It still contains inherited Breezy wording and is
  not Solstice's finished privacy notice. Use the data map as evidence, not as legal approval.
- [ ] Correct the custom `READ_PROVIDER` permission label and description; it still uses Breezy
  wording. Treat this as visible app copy and follow the approval rule.
- [ ] Confirm telemetry and data contribution remain off by default; any future opt-in must be
  explicit, informed, separate from core functionality, and revocable.
- [ ] For every proposed data flow, state payload, purpose, destination/recipients, retention,
  controls, and provide an inspectable example before implementation.
- [ ] Check provider privacy links, attribution, licences, inherited notices, and LGPL-3.0
  obligations against what the app actually ships.
- [ ] Review Actions permissions, secret exposure, signing, artifact provenance, release replacement,
  dependency pinning, and untrusted pull-request execution.
- [ ] Keep signing credentials out of PR jobs, logs, artifacts, caches, and broad job environments.
- [ ] Do not weaken privacy or security requirements for lower CI cost or faster execution.

## 3. Gradle, CI, and release reproducibility

- [x] Inventory active workflows: `.github/workflows/build.yml` is the active build workflow.
- [x] Enable Gradle local build caching and keep `gradle/actions/setup-gradle` as the single cache
  owner.
- [x] Consolidate formatting, app/dependency unit tests, Debug assembly, and eligible main-only
  Profile assembly into the intended Gradle task graph.
- [x] Add same-PR/branch concurrency cancellation for obsolete runs.
- [x] Separate least-privilege release publication and scope signing secrets to protected-main work.
- [x] Restrict temporary signing-file permissions and pin artifact actions to verified immutable SHAs.
- [x] Add the diff-based no-hardcoding guard with 17 built-in self-tests and a narrowly scoped,
  owner-reviewed allowlist. It is a heuristic, not a Kotlin parser; manual review remains mandatory.
- [x] Verify PR #14 exact head `31cace1d3166896871bb0e9ede8182f3b4c29aad` on Build run
  #285 (`45905434026`): formatting, no-hardcoding checks, unit-test task graph, Debug assembly,
  and Debug APK artifact upload passed.
- [ ] Re-run and inspect the same checks on the final updated PR head after this documentation cleanup.
- [ ] Verify Profile signing, testing APK artifact handoff, and testing-release publication on
  protected `main` with configured secrets. PR CI intentionally skips those steps.
- [ ] Re-check JDK, Gradle Wrapper checksum, Android Gradle Plugin, Kotlin, SDK/build-tools, and
  action versions on the final head.
- [ ] Measure several comparable cold/warm runs, cache hits, job durations, artifact sizes, and
  failure/retry rates. Existing samples are mixed; no stable time or runner-cost saving is claimed.
- [ ] Record physical-device and Android ICU checks as NOT TESTED or DEFERRED unless actually run.

## 4. AI task sizing and cost controls

- [x] Document one primary agent per coherent task and evidence-based handoffs.
- [x] Set Sonnet or a balanced ChatGPT/Codex coding model as the default for ordinary implementation,
  tests, debugging, and bounded documentation.
- [x] Define when independent Opus review is recommended for difficult architecture, security,
  privacy, licensing, high-risk CI/release, or broad refactors.
- [x] State that Opus is optional and does not authorize paid usage.
- [ ] Obtain independent review of CI credential/release permissions when available and authorized.
- [ ] Do not use paid APIs, extra credits, subscriptions, tools, or tiers without explicit approval.
- [ ] Report model/tool usage and costs only when known; never infer them from message counts.

## 5. Final review and closure

- [ ] Inspect the complete final diff for contradictions, duplicated authority, stale references,
  and implementation tasks that belong outside the Solstice app.
- [ ] Check Markdown rendering and all links to the new integration boundary and surviving docs.
- [ ] Confirm deleted dependency plans/specs have no remaining active inbound links.
- [ ] Ensure all remaining app privacy, release, accessibility, and device gaps are either completed
  or explicitly marked deferred with evidence and an owner decision.
- [ ] Review the final PR scope and exact-head CI before requesting owner review.
- [ ] Keep the follow-up cleanup PR unmerged until the owner review and outstanding acceptance gates are resolved or explicitly deferred.

## 6. Confirmed boundaries

- Solstice's active plans describe only the app's requirements, behavior, implementation, tests,
  privacy, and release work.
- A dependency's internal modules, code, lab, API design, technical decisions, milestone sequence,
  and platform expansion are documented and implemented in that dependency's own repository.
- Solstice documents only the consumer requirements it owns: compatible public API/artifact, pinned
  version, licence/notices, Android support, privacy implications, lifecycle, errors, and parity
  testing.
- A dependency validation harness is not a production engine or a released consumer API.
