# Solstice — AI Cost and Resource Efficiency Policy

**Status:** Project policy  
**Scope:** AI-assisted planning, coding, research, repository operations, and verification for Solstice.  
**Related authority:** `docs/RULES.md`, root `AGENTS.md`, and the shared [AI Usage and Cost Policy](https://github.com/INFORMATION89/Motion-UI/blob/main/docs/AI_USAGE_POLICY.md).

## 1. Governing principle

Use the least costly workflow that provides sufficient evidence for the task. Never reduce correctness, privacy, security, accessibility, licensing, regression coverage, or release confidence merely to save resources.

Cost includes model usage and subscription allowances, API/tool credits, repository and web requests, GitHub Actions minutes, emulator/device time, weather-provider calls, downloaded datasets, local CPU/memory/disk/network, and human review/rework.

Do not invent a budget or monetary cost. Report actual usage only when exposed by the tool; label estimates as estimates.

## 2. Model and task assignment

Use one primary owner for each coherent task. Model names and availability change; choose by task risk and actual account access, not by a fixed model entitlement.

| Task | Recommended owner / model | Opus second pass |
|---|---|---|
| Formatting, short documentation edits, extraction, simple mechanical changes | Efficient model; Sonnet for bounded implementation | Not normally useful |
| Normal Android/Compose implementation, tests, focused debugging, ordinary review | Claude Sonnet or ChatGPT/Codex with proportionate reasoning effort | Only if the diff has meaningful hidden risk |
| Cross-repository coordination, complex Gradle/CI diagnosis, difficult animation/concurrency bugs | ChatGPT/Codex or Sonnet as primary, with one clear owner | **Recommended** when an independent advanced review can challenge assumptions |
| Security, privacy, licensing, data handling, major architecture, public API or migration decisions | ChatGPT/Codex or Sonnet prepares evidence and alternatives; owner approves strategic decisions | **Strongly recommended** as an independent review when available and approved |
| Release, protected-branch, destructive, paid-service, or irreversible action | Explicit owner authorization required | Review does not replace authorization |

### ChatGPT/Codex and Claude collaboration

- ChatGPT may coordinate repository work, maintain cross-repository context, break work into bounded tasks, inspect GitHub, implement, and consolidate verification.
- Claude Sonnet is the default recommendation for ordinary coding and focused implementation.
- ChatGPT/Codex may start and own a task; do not wait for Opus before beginning.
- Add a handoff label **“Opus review recommended”** for complex or high-impact tasks. Give Opus the concise specification, relevant diff, tests/evidence, constraints, and unresolved questions at a useful checkpoint or before merge.
- Opus is an optional second reviewer, not the default model for every task, not proof of correctness, and not a blocker. Verify every substantive finding.
- If Opus is unavailable or would require unapproved paid usage, continue with proportionate ChatGPT/Codex and Sonnet review and disclose that the Opus pass was deferred.
- Do not ask two agents to repeat repository exploration, implement the same change independently, or run identical full CI matrices by default.

## 3. Subscription, API, and paid-usage boundaries

- Subscription allowances, prepaid credits, extra usage, and API-key billing are separate systems. Never estimate subscription consumption from public API token prices.
- Treat included usage as finite. If exact usage is not visible, say it is unknown.
- Do not enable paid APIs, extra credits, auto-reload, paid overages, new subscriptions, paid runners, or higher-cost tiers without explicit owner approval.
- Never silently switch from subscription usage to API billing or another provider when a quota is reached.
- Do not use production credentials or private keys for routine tests.

## 4. Context, tools, and repository efficiency

- Start from the active task, acceptance criteria, mandatory rules, and only the relevant files.
- Search narrowly and fetch targeted ranges before reading large documents or logs.
- Reuse verified findings only when the relevant branch, SHA, files, toolchain, and environment have not changed.
- Keep durable handoffs concise: branch, SHA, approved scope, decisions, changed files, verification, limitations, and next action.
- Batch independent read-only lookups when useful; keep dependent writes sequential.
- Stop repeated searches, polling, unchanged retries, looping agents, and scope expansion.
- Prefer deterministic scripts, Gradle tasks, formatters, and fixtures for mechanical verification over asking a model to infer tool output.
- Stop investigating when evidence is sufficient to make and verify the requested decision.

## 5. Cost-aware CI and Gradle verification

Select checks according to changed risk. Do not run the full Android build for a documentation-only change unless the documentation modifies executable CI/build configuration.

| Change | Proportionate verification |
|---|---|
| Documentation only | Markdown/link checks and focused diff review |
| Kotlin logic or isolated UI component | Relevant unit tests, lint/format checks, affected module compile |
| Compose/UI, resources, navigation, accessibility | Relevant tests, resource/build checks, and targeted emulator/device verification when available |
| Gradle wrapper, plugins, dependencies, build logic, convention plugins | Wrapper validation, dependency/Gradle configuration checks, affected module builds, relevant CI matrix |
| Shared app architecture, flavors, manifest, packaging, release workflow | Full required Android build/test matrix and packaging checks |
| Security, privacy, licensing, signing, release or supply-chain changes | All applicable mandatory security and release gates; no cost-based weakening |

### Gradle and CI efficiency rules

- Use the Gradle Wrapper committed by the repository; do not install an unrelated system Gradle version.
- Prefer the maintained `gradle/actions/setup-gradle` action for Gradle distribution and dependency/build caching, with cache keys and write permissions scoped safely.
- Configure Gradle caching once per job and avoid competing cache implementations that duplicate the same Gradle user-home data.
- Cache only reproducible Gradle state. Do not cache signing keys, secrets, mutable build outputs that can hide stale inputs, or user-specific configuration.
- Use dependency verification/locking where the project already supports it; do not introduce broad dependency changes as incidental CI cleanup.
- Avoid redundant Gradle invocations in separate steps when one invocation can run the required task set.
- Use path-aware job selection only when dependency boundaries are reliable; changes to shared build logic, version catalogs, wrapper, plugins, or convention plugins must trigger all affected jobs.
- Reuse successful checks only for the same relevant SHA, toolchain, runner, and inputs. After a failure, inspect the failing job and rerun only failed jobs when supported.
- Do not retry unchanged infrastructure failures repeatedly. Diagnose, make one meaningful adjustment, and report BLOCKED if it persists.
- Keep required checks as merge gates. A green CI result does not prove physical-device behavior; report device checks as NOT TESTED or DEFERRED when not run.
- For production Kotlin changes, run the required diff-based no-hardcoding guard. It checks common literal patterns in added lines only; it is a heuristic, not a full Kotlin parser. Review the entire diff and require owner approval for allowlist exceptions.

### Gradle container images

A Gradle Docker image is not automatically faster or more complete than a GitHub-hosted runner. Before adopting one, inspect the actual Solstice workflows and compare runner setup, Android SDK/build-tools availability, JDK, emulator support, cache behavior, image maintenance, and startup/pull time.

- Do not move Android emulator jobs into a generic `gradle:<version>-jdk...` image unless the image also provides the required Android SDK, build-tools, emulator, and KVM/device support.
- For ordinary Android CI, prefer a supported GitHub-hosted Ubuntu runner with a pinned JDK, Android SDK setup, the repository Gradle Wrapper, and `setup-gradle` caching unless measurements show a better maintained image-based approach.
- A custom prebuilt image is justified only when it removes substantial repeated setup, is reproducibly built and maintained, is pinned by immutable digest, has a documented update process, and demonstrably improves total runtime/cost.
- Never sacrifice runner compatibility, supply-chain integrity, or test coverage to use a container image.
- Measure cold and warm runs before and after; report wall-clock time, cache hit/miss, runner minutes, and any extra image-maintenance cost. Do not claim savings without measurements.

## 6. Weather data and external services

- Routine tests use deterministic fixtures and do not call live weather providers.
- Live requests are limited to cases where external behavior cannot be established offline; keep them manual or scheduled, minimally scoped, and within provider terms and quotas.
- Record source, retrieval date, licence, and sanitization for retained fixtures.
- Do not repeatedly download the same data or expand historical datasets without a defined research question and approved scope.
- Provider/API usage is distinct from AI usage and CI cost.

## 7. Bounded execution and stop conditions

For multi-step tasks, define objective, deliverables, acceptance criteria, allowed scope, checkpoints, and stop conditions. Complete required work before optional cleanup. Stop when acceptance criteria are met or when a required approval, quota, unavailable runner, or missing evidence blocks progress. Do not conceal incomplete verification.

## 8. Owner approval

Ask before changing product direction or visible behavior; privacy, telemetry, consent, or retention; security exceptions or credentials; paid usage or recurring services; protected branch rules; destructive migrations; releases/publication; or any required verification gate.

Routine choices such as selecting focused tests, narrowing a search, reusing valid caches, and choosing a local deterministic check do not require separate approval when within the agreed scope.

## 9. Reporting

For every non-trivial task or PR, report:

- objective and acceptance criteria;
- primary agent/model and any independent reviewer, only when known;
- files changed and exact branch/commit/PR;
- tests and CI jobs run, with exact outcomes and SHA;
- checks not run and why;
- actual external/paid usage only when known;
- cache/image measurements for CI optimization;
- risks, blockers, and next action.

Never invent token counts, dollar amounts, cache savings, test results, or device coverage.

## 10. Relationship to other documents

- `docs/RULES.md` remains authoritative for Solstice product, design, privacy, clean-room, and engineering constraints.
- Root `AGENTS.md` gives coding agents their operational reading order and workflow.
- This file defines Solstice-specific AI/resource efficiency and verification guidance.
- The shared AI usage policy provides baseline guidance. This document adds Solstice/Android/Gradle-specific rules. This document adds Solstice/Android/Gradle-specific rules.
- `docs/PLAN.md` owns product and architecture decisions; `docs/ROADMAP.md` owns remaining work; `docs/CONTINUE.md` owns the current handoff.
