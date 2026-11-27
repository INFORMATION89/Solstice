# Solstice engineering rules

These rules govern work in the Solstice application repository only. These rules come before speed and before any task.
They change only with the project owner's approval. If a task seems to need breaking one, stop and ask.

## 1. Ask first, always

1. **Plans are made in chat with the project owner.** Coding sessions only carry out what's agreed in
   [PLAN.md](PLAN.md), [ROADMAP.md](ROADMAP.md) and [CONTINUE.md](CONTINUE.md).
2. **Never start building anything new without asking**: no new feature, screen, card, button, text,
   setting, motion, haptic, token or system. Say exactly what would change, show options when there
   are choices, and wait for a yes.
3. **Check guidelines with the project owner before relying on them.** When Apple's, Google's or our own rules
   disagree, or a rule doesn't cover the case, bring it to the project owner; don't pick on your own.
4. When something agreed turns out not to work as planned, stop and report; don't improvise a
   different solution.

## 2. App and dependency boundaries

1. Solstice owns its Android application, screens, weather presentation, saved places, permissions, lifecycle, refresh scheduling, notifications, user-facing recommendations, privacy choices, and product decisions.
2. Motion-UI and Meridian-Engine are independent projects. Their implementation, architecture, releases, internal roadmaps, and tests belong in their own repositories.
3. Solstice consumes only APIs and artifacts present in the exact reviewed dependency revision. Pin dependencies, review licences and public APIs, and test the app-side integration before updating a pin.
4. A reusable capability that belongs in an external dependency must be proposed and implemented there first. Solstice records only its consumer requirements, integration contract, and app-side adoption.
5. Solstice is being reconstructed as an independent application. Do not introduce new Breezy code or copy upstream implementation. Existing inherited files remain subject to the app audit and replacement plan; preserve their applicable licence notices until they are removed.

## 3. Never hardcode anything

1. **No typed-in values** in components or app code: no `dp`, `sp`, colours, alphas, corner radii,
   durations, `delay()`s, springs, thresholds, fractions or counts. Every value is either a **token** or
   **derived** (from layout, data, insets or another token).
2. **No typed-in text:** every string is a resource; English first, other languages marked as needing
   an update, never machine-translated.
3. A value with no token yet: **stop, propose a name and value, ask**, then add it to the tokens. Don't
   add it "for now".
4. Allowed without a token: 0, 1, mathematical constants, and the entries in the reviewed allowlist.
   CI enforces this; the allowlist only grows with the project owner's approval.
5. No special cases by screen ("if in a window…"). Differences are configuration (data), passed in.

## 4. Data, sources and privacy

For app-owned provider coverage and attribution, use [COVERAGE.md](COVERAGE.md). External dependency boundaries are in [INTEGRATIONS.md](INTEGRATIONS.md).

**Privacy**

1. The public [PRIVACY.md](../PRIVACY.md) describes data flows Solstice has identified. The internal [PRIVACY_DATA_MAP.md](PRIVACY_DATA_MAP.md) records code evidence and unresolved audit questions.
2. Never claim that no personal data is processed, that all data stays on-device, or that third-party providers do not retain requests unless complete, current evidence supports the exact claim.
3. Distinguish device location, IP-derived location, search and reverse-geocoding requests, weather-provider requests, optional update checks, local app-to-app sharing, and user-initiated log exports. Do not conflate local sharing with remote telemetry.
4. A provider receives the fields required for the selected feature and ordinary network metadata. Document the actual endpoint, payload, purpose, recipient, provider policy, and known retention before making a more specific public claim.
5. Optional, non-essential remote sharing must be off by default, separately explained, explicit, revocable, and independent of unrelated settings. Do not describe a proposed contribution pipeline as implemented.
6. Do not add analytics, tracking, advertising, social features, or new data recipients without explicit product-owner approval and a documented privacy review.
7. Treat permissions and exported Android components as disclosure boundaries. Their prompts, labels, receiver selection, and access controls must accurately describe the data exposed.
8. Keep the privacy notice and data map synchronized whenever a data flow, provider, permission, build flavor, or sharing feature changes.

**Sources**
1. Each source declares its coverage, tier, terms and required attribution as data, not code.
2. Every result carries its provenance: which sources, their weights, any correction, how fresh each
   part is. What the app shows about sources comes from this, never from guesses.
3. No single source can break a forecast: if one fails, the next tier fills in and the provenance
   says so. Sources that read a website instead of an API are best-effort and never the only source.
4. A source's terms are read in full before it's added; required credits are shown where its data
   shows. Keys belong to the person and stay on the phone, never in the repo or the build.
5. Sources are tested offline with recorded responses, and changes to the blend are verified against
   public archives before they ship.

## 5. Clean-room development and provenance

Solstice is being reconstructed as an independent application. Meridian-Engine and Motion-UI are
independent projects with their own repositories and ownership. These rules govern only the
Solstice work performed in this repository.

1. **Do not introduce copied upstream implementation.** New Solstice code must be original work
   based on public specifications, documented behavior, platform APIs, and approved dependencies.
   Do not copy, translate, or closely paraphrase Breezy Weather or another project's source.
2. **Existing inherited code is not automatically approved for reuse.** Keep it in the audit scope;
   classify it for removal, replacement, or an explicitly justified temporary retention. Preserve
   applicable licence and attribution notices while inherited code remains.
3. **Allowed references:** public specifications and standards (CAP, METAR, WMO), service API
   documentation and terms, published research, platform documentation and guidelines, our own
   project documents, and externally observable behavior. Use libraries through their public APIs
   under their licences; do not paste their implementation into Solstice.
4. **Spec, then build:** record the intended behavior and allowed sources in Solstice's own words
   before implementing a substantial new component. Keep implementation tasks separate from
   source-code inspection when a clean-room boundary is required.
5. Each pull request records material sources consulted and identifies any inherited files changed.
   If new code is found to be too close to an upstream implementation, replace it rather than
   rationalizing the similarity.
6. This process reduces provenance risk but cannot eliminate the possibility that AI models have
   encountered public source code during training.

## 6. Platform guidelines

Android first in structure, iOS quality in feel. Check both before proposing anything.

**Material 3 (Android):** system back and predictive back everywhere; edge to edge; 48 dp touch
targets; Material Symbols; the type scale; dynamic colour roles; Material components where they exist;
the Material `MotionScheme` fed by our tokens.

**Apple HIG (feel and judgement):** fluid interfaces (respond at once, interrupt and redirect, hand over
velocity, project flings, rubber-band); a sheet keeps its source and closes with a swipe; glass is for
controls and navigation; motion is purposeful and brief on frequent actions; haptics consistent,
short and never the only feedback.

**Accessibility, not negotiable:** 48 dp targets with room between them; text up to 200 %; contrast
4.5:1 for body text, 3:1 for large text, on every sky; never colour alone; every gesture also has a
button or a TalkBack action; nothing that disappears on a timer alone; RTL mirrored.

**Decided exceptions** (project owner): the ✕ sits at the end of headers; the toolbar keeps its current
animation; glass on cards is under review. Exceptions are listed here and in PLAN.md, nowhere else.

## 7. Motion

Full rules in [MOTION.md](MOTION.md). In short:
- Springs only, from tokens. Movement and size use spatial springs; opacity, colour and blur use
  effects springs, which never bounce.
- Things morph, never pop; one material per morph; the button becomes the card; closing goes back
  into the source.
- Nothing blocks input; gestures drive a motion value and hand their velocity to the release.
- **Reduce Motion** ("Remove animations"): every Motion piece has a reduced form, agreed with the project owner
  (Apple's list: fades instead of moves, no blur hand-offs, no depth, less bounce, follow the finger).
- Haptics from tokens only; they follow the system setting; none added without agreeing them.
- Every new or changed motion is proposed with options and agreed before it's built.

## 8. Design

- [DESIGN.md](DESIGN.md)'s checks apply to every proposal: one screen one job, someone needs it,
  essentials first, every state designed (loading, empty, error, offline), any size and orientation.
- **Skeuomorphism lives in interaction, not in looks** (physics, press, drag, haptics); visuals stay
  clean and standard.
- [WRITING.md](WRITING.md) for every sentence; every new sentence goes to Marco before it ships.

## 9. Done means checked

A change is done when:
1. CI passes the formatting, no-hardcoding, relevant app/dependency unit-test, and required build checks;
2. app-owned logic has unit tests; app-side Motion-UI behavior is covered by the consumer integration contract and tests; Motion Lab demos and Motion-UI API documentation remain owned by the Motion-UI repository;
3. it was checked on the Pixel (profile build for smoothness; frame stats when motion changed), or is
   clearly marked "not checked on the phone";
4. the docs are updated: PLAN.md (decisions), LOG.md (a dated line), and ROADMAP.md (ticked). Dependency-owned documentation changes belong in the dependency repository.

## 10. Docs

One source of truth per topic: PLAN.md decisions, LOG.md history, ROADMAP.md to-dos, CONTINUE.md
where things stand, DESIGN/MOTION/WRITING the rules, this file how we work. Fix contradictions
instead of adding to them. Never push upstream's history; never build with `-Pbreezy`.

## 11. Git

1. Work on a branch and merge to `main` through a pull request; nothing is pushed to `main` directly.
2. Commit as INFORMATION89. Never commit `gradlew.bat` (it shows as changed on Linux checkouts),
   secrets, keys or `local.properties`.
3. A pull request says what changed and what was checked (section 9). Once CI exists, `main` is
   protected and merges need it to pass.


## 12. AI tools, model selection and cost

AI-assisted development must also follow the shared [AI Usage and Cost Policy](https://github.com/INFORMATION89/Motion-UI/blob/main/docs/AI_USAGE_POLICY.md). It supplements these Solstice-specific rules; it does not override the requirement to agree on user-visible product and design changes.

- Use the least costly model that can reliably complete the task. Routine documentation, extraction and mechanical edits use an efficient model; ordinary implementation and debugging use a balanced coding model; complex architecture, difficult debugging and high-risk reviews justify a stronger reasoning model.
- ChatGPT Work/Codex subscription allowances and OpenAI API billing are separate. Do not estimate subscription usage from API token prices or assume access to a model or plan.
- Use Claude Haiku-class models for bounded routine tasks, Sonnet-class models for normal engineering, and reserve Claude Opus 5.5 for complex architecture, long-running coding, difficult debugging, broad refactors or independent review where its additional capability materially reduces risk or rework.
- Treat model choice as guidance, not a blocker: ChatGPT/Codex may begin and own the task. When a stronger independent pass would help, mark **“Opus review recommended”** and leave that review for a useful checkpoint or before merge. Do not wait for Opus before starting, and do not incur extra paid usage without approval; if the review is not performed, say so.
- One primary agent owns each coherent task. Add a second agent only for an independent review or specialist contribution; do not duplicate repository exploration, implementation or full CI runs by default.
- Keep context focused: inspect relevant files and diffs, avoid repeating large reads and logs, reuse evidence only for the same relevant commit, and maintain concise handoffs.
- Stop agents that loop, repeat unchanged searches or tests, or expand scope without approval.
- No new paid API, credits, subscription, paid tool, higher-cost tier or open-ended spend without explicit owner approval. Never silently switch from subscription usage to API billing.
- Cost-aware verification must not weaken correctness, privacy, accessibility, security, regression coverage or required release checks. For the Pixel/device requirement in section 9, report honestly when a physical-device check is unavailable; a green CI run is not a phone check.
- Report actual model/tool usage only when known. Never invent token counts or monetary costs.
