# Solstice engineering rules

These rules govern work in the Solstice application repository only. They do not define the roadmap,
architecture, API, or implementation process of an external dependency. They change only with owner
approval; when a product decision is missing, stop and present the specific choice.

## 1. Product approval

1. Product direction and visible changes are agreed in chat and recorded in [PLAN.md](PLAN.md).
2. Do not introduce a new screen, feature, setting, interaction, text, haptic, token, or user-visible
   behavior without approval. Describe the change and its impact before implementing it.
3. When an agreed implementation does not work, report the evidence and propose an app-side
   alternative rather than silently changing the product decision.
4. Keep [ROADMAP.md](ROADMAP.md) limited to Solstice-owned app work. A dependency's feature backlog,
   lab, internal milestone, API design, storage/network choice, or platform plan does not belong here.

## 2. App architecture and external dependencies

1. Solstice owns its product identity, weather experience, screens, navigation, app state, saved
   places, settings, permissions, refresh scheduling, notifications, accessibility, copy, and
   practical recommendations.
2. Use stable, documented APIs from Compose, Material 3, Motion, and other maintained libraries for
   reusable mechanics. Do not duplicate a dependency's internal implementation merely to avoid using
   its API.
3. An external dependency remains independently owned. Do not place its implementation tasks,
   roadmap, milestones, lab work, API proposals, storage/network choices, or release plan in Solstice
   documents. Record only the consumer-side contract and integration constraints in
   [INTEGRATIONS.md](INTEGRATIONS.md).
4. Integrate only an exact reviewed dependency commit or released artifact. Check the actual public
   API, licence, platform requirements, transitive dependencies, and tests before changing the pin.
   Never depend on an unmerged branch or an undocumented API.
5. Solstice must remain buildable and useful without a not-yet-approved future dependency. Do not
   describe a planned integration as shipped.
6. Do not copy Solstice screens, business logic, weather models, user preferences, branding, or
   resources into generic dependencies. Do not copy dependency implementation code into Solstice.
7. New app UI code belongs in the established Solstice UI structure. Prefer existing app systems and
   tokens over one-off components.

## 3. Values and text

1. Do not hardcode dimensions, colours, alpha, corner radii, animation timings, thresholds, fractions,
   or other design-system values in UI call sites. Use a named Solstice token or derive the value from
   layout, data, insets, or another token.
2. Do not hardcode user-facing text. Use string resources; add English first and mark translations
   for a human update rather than machine-translating them.
3. If a required token or string is missing, identify the name and proposed value/text before adding
   it. Exceptions require owner approval.
4. The CI no-hardcoding scan is a deterministic heuristic over added production Kotlin lines, not a
   Kotlin parser or proof that all hardcodes are detected. Review the full diff; allowlist exceptions
   require approval.

## 4. Weather data, sources, and privacy

1. Solstice may request the weather/location data needed for its core forecast experience. Any
   additional collection or contribution is off by default and requires separate, explicit, informed,
   revocable opt-in.
2. A contribution/consent screen must identify the actual payload, purpose, destination and recipients,
   retention, user controls, and an inspectable example. Refusal must not reduce core functionality.
3. Minimize data. Do not add accounts, device/ad identifiers, analytics, tracking, ads, or social
   behavior without a separate product and privacy review.
4. Every active provider used by the app must have a verified coverage, terms, privacy, and attribution
   record. Show required credits where the relevant data is presented.
5. Keep credentials out of source control and distributed build configuration unless the provider's
   public-client model explicitly permits it. Never expose private developer credentials in artifacts.
6. Test app-side provider adapters and mapping behavior with deterministic fixtures where feasible.
   Meteorological blending, bias correction, learning algorithms, and engine-level provider strategy
   belong to the independent weather-engine project, not this app ruleset.
7. Keep weather evidence distinguishable from the advice Solstice derives from it. Clothing and
   footwear, umbrella and sunscreen/shade advice, activity and outdoor planning, personal preferences,
   alert thresholds, cooldowns, wording, and notification delivery are app-owned decisions.

## 5. Source provenance, licensing, and fork boundaries

1. Solstice is an application fork. Inherited Breezy-derived source remains within this repository
   under its applicable licence and notices; do not transplant it into independent dependencies.
2. Preserve upstream copyright, licence headers, third-party notices, and attribution when modifying
   inherited code or adding dependencies. Review compatibility before introducing a new dependency.
3. Do not copy implementation code from external projects into Solstice. Consume documented public
   APIs; report a missing capability to the relevant dependency owner.
4. Keep new app behavior and Solstice-specific adaptations in this repository. Do not make an
   external dependency require a Solstice checkout, service, package, theme, data model, or lifecycle.

## 6. Android platform and design guidelines

Android is the supported app platform. Do not imply that Solstice gains iOS or desktop support because
a library consumed by the app supports other platforms.

**Material 3:** system back and predictive back; edge-to-edge; accessible touch targets; Material
Symbols; dynamic colour roles; the type scale; Material components where appropriate; and a motion
scheme configured with Solstice's design choices.

**Interaction quality:** respond promptly, remain interruptible, preserve gesture ownership and
velocity handoff, and avoid swallowing input. Use a sheet that remains connected to its source where
that is the approved app design. Haptics are supplementary feedback, never the only feedback.

**Accessibility:** accessible touch targets and spacing; text scaling; contrast on every sky; never
communicate by colour alone; every gesture must also have an accessible action; avoid time-only
disappearing content; mirror layouts for RTL.

## 7. Solstice motion behavior

The full app-specific motion reference is [MOTION.md](MOTION.md). Key rules:

- Keep the current Solstice toolbar animation and its bud as part of the app's product identity.
- Interactions should be interruptible where appropriate and must not delay or block input.
- A surface that opens from a control should visually retain its relationship to that source.
- The user must be able to stop, reverse, or redirect an interaction without waiting for an animation.
- Respect Android's reduced-motion/system animation settings. Reduced motion should preserve state
  clarity and direct manipulation while reducing spatial movement and decorative effects.
- Do not add haptics or change the animation feel without product approval.

## 8. Definition of done

A change is done when:

1. Relevant configured CI checks pass on the exact commit, including formatting, app/library unit
   tests, and the required Debug build/artifact path.
2. App logic has focused regression tests. UI behavior has appropriate Compose/instrumentation tests
   where feasible.
3. Physical-device checks are performed for interaction/visual changes when hardware is available.
   Otherwise mark them **NOT TESTED**; CI is not a substitute for a phone check.
4. Privacy, accessibility, provenance, licence, and failure/offline behavior are checked where the
   change affects them.
5. The complete diff is reviewed and app documentation is updated: PLAN for decisions, LOG for dated
   history, ROADMAP for app tasks, CONTINUE for the current handoff. Dependency owners update their
   own repository documentation for dependency implementation/API changes.

## 9. Documentation authority

One source of truth per topic:

- [PLAN.md](PLAN.md): current Solstice product decisions.
- [ROADMAP.md](ROADMAP.md): Solstice-owned app tasks only.
- [LOG.md](LOG.md): chronological history, including superseded historical decisions.
- [CONTINUE.md](CONTINUE.md): current actionable app handoff.
- [DESIGN.md](DESIGN.md), [MOTION.md](MOTION.md), [WRITING.md](WRITING.md): Solstice experience and
  design rules.
- [INTEGRATIONS.md](INTEGRATIONS.md): consumer-side dependency contracts only.
- [PRIVACY_DATA_MAP.md](PRIVACY_DATA_MAP.md): internal technical evidence inventory, not a public
  privacy notice.
- Root [AGENTS.md](../AGENTS.md): AI agent operating instructions.
- [AI_COST_POLICY.md](AI_COST_POLICY.md): AI, tool, and CI cost controls.

Fix contradictions in the authoritative file; do not add another competing document.

## 10. Git and CI

1. Work on a branch and merge to `main` through a pull request; do not push ordinary work directly to
   `main`.
2. Commit as INFORMATION89. Never commit `gradlew.bat`, secrets, keys, signing material, or
   `local.properties`.
3. Pull requests describe what changed, why, what was checked, what was not tested, and the exact
   commit SHA.
4. The active `Protect main` ruleset requires a pull request and the `build` status check, blocks
   force pushes/deletion, and has no bypass actors. Keep the ruleset aligned if workflow check names
   change.

## 11. AI tools, model selection, and cost

AI-assisted development must follow root [AGENTS.md](../AGENTS.md), the local
[AI_COST_POLICY.md](AI_COST_POLICY.md), and the shared
[Shared AI Usage and Cost Policy](https://github.com/INFORMATION89/Motion-UI/blob/main/docs/AI_USAGE_POLICY.md).

- Use Sonnet or a balanced ChatGPT/Codex coding model as the default for ordinary implementation,
  tests, debugging, and bounded documentation.
- Use a stronger reasoning model such as Opus only when independent advanced review or difficult
  architecture/root-cause work is likely to materially reduce risk.
- Opus is optional, not an automatic gate and not permission for paid usage. Record a recommendation
  as pending if it has not happened; do not block normal work waiting for it.
- One primary agent owns each coherent task. Use a second agent for an independent review, not to
  duplicate repository exploration or full CI by default.
- Keep context focused, inspect exact diffs, reuse CI evidence only for the same relevant SHA, and
  report model/tool usage only when known.
- No new paid API, credits, subscriptions, paid tools, or open-ended spend without explicit approval.
- Cost-aware verification must not weaken correctness, privacy, accessibility, security, regression
  coverage, or required release checks.
