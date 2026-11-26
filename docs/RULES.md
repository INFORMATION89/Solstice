# Rules

How every session works on Solstice, Motion, and Meridian. These rules come before speed and before any task.
They change only with Marco's OK. If a task seems to need breaking one, stop and ask.

## 1. Ask first, always

1. **Plans are made in chat with Marco.** Coding sessions only carry out what's agreed in
   [PLAN.md](PLAN.md), [ROADMAP.md](ROADMAP.md) and [CONTINUE.md](CONTINUE.md).
2. **Never start building anything new without asking**: no new feature, screen, card, button, text,
   setting, motion, haptic, token or system. Say exactly what would change, show options when there
   are choices, and wait for a yes.
3. **Check guidelines with Marco before relying on them.** When Apple's, Google's or our own rules
   disagree, or a rule doesn't cover the case, bring it to Marco; don't pick on your own.
4. When something agreed turns out not to work as planned, stop and report; don't improvise a
   different solution.

## 2. Motion and frameworks first, then the app

1. **Reusable interaction and motion behavior belongs in Motion** first, is tried in the Motion Lab where available, then the app imports and **configures** it. Reusable meteorological engine capabilities belong in Meridian-Engine under its own plan and approval gates; Solstice consumes its approved public contract. Solstice retains app-specific behavior, configuration, and weather presentation.
2. Motion never imports app code. A change Motion needs lands in Motion, then Solstice bumps the
   submodule (CONTINUE.md, "Motion").
3. **Use what exists before writing anything**, in this order: Motion's pieces and tokens → Material 3
   components → a maintained library (check its licence) → new code, only after asking.
4. **One of each system.** One page frame, card, sheet, toolbar, message, button style, text role set,
   glass. If a system lacks something, extend the system; never build a one-off beside it.
5. New code in the app goes in `ui/expressive/`; edits to upstream files stay small and are listed in
   PLAN.md.
6. **The weather engine works like Motion:** its own library, no UI code and no Solstice code in it;
   Solstice imports and configures it. Written clean room (section 5).

## 3. Never hardcode anything

1. **No typed-in values** in components or app code: no `dp`, `sp`, colours, alphas, corner radii,
   durations, `delay()`s, springs, thresholds, fractions or counts. Every value is either a **token** or
   **derived** (from layout, data, insets or another token).
2. **No typed-in text:** every string is a resource; English first, other languages marked as needing
   an update, never machine-translated.
3. A value with no token yet: **stop, propose a name and value, ask**, then add it to the tokens. Don't
   add it "for now".
4. Allowed without a token: 0, 1, mathematical constants, and entries in the reviewed allowlist.
   The required `No hardcoded UI literals` step scans added production Kotlin lines for common
   dimensions, colors, alpha, animation timing, thresholds/counts/comparisons, and direct UI text.
   It excludes test sources and the canonical `theme/Tokens.kt` token-definition file. This is a
   deterministic heuristic, not a Kotlin AST or proof that every possible hardcode is caught;
   review the full diff. Allowlist exceptions require Marco's OK.
5. No special cases by screen ("if in a window…"). Differences are configuration (data), passed in.

## 4. Data, sources and privacy

Design in [SOURCES.md](SOURCES.md).

**Privacy**
1. Nothing leaves the phone except the weather requests needed for a forecast, unless the person
   opted in.
2. Opt-in is asked once during onboarding, off by default, and can be changed any time in settings.
   Saying no changes nothing else in the app.
3. The opt-in screen shows the actual data that would be sent (a real example from that phone) and a
   visualization of its path: phone → what's removed (place, time, identifiers) → our server → what's
   done with it → what comes back to everyone.
4. Data is minimized before it leaves the phone: forecast error only, the area rounded to a coarse
   cell, time rounded to the hour. No account, no device or advertising ID, no location history, no
   raw coordinates. Cell size and rounding are tokens.
5. The server uses an area only once enough phones have contributed there; below that, nothing about
   the area is kept. The threshold is a token.
6. Contributions are kept for a short, stated time; the server code is open source; applicable law
   (Mexico's LFPDPPP, the EU's GDPR) is checked before the server exists.
7. No analytics, no tracking, no ads, no social features without Marco's OK.

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

## 5. Clean room (agreed 2026-11-23)

Applies to the weather engine, Motion and future apps; the Solstice app itself stays a Breezy fork
until Marco decides otherwise.
1. **Never copy, translate or closely paraphrase another project's code**, Breezy included, into
   these projects. Ideas, algorithms, public standards and data formats are fine.
2. **Allowed sources:** public specs and standards (CAP, METAR, WMO), each service's own API docs and
   terms, published papers, platform docs and guidelines, our own docs, and how things behave from
   the outside. Libraries are used through their API, under their licence, never pasted in.
3. **Spec, then build:** one session writes a spec in our own words from allowed sources and saves it
   in the project's docs; a separate session builds only from that spec and never opens the original
   code for that part.
4. Each pull request lists what was consulted. Code that looks close to an existing project gets
   rewritten, not justified.
5. Honest limit: AI models have seen public code, so this lowers the risk rather than removing it.

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

**Decided exceptions** (Marco): the ✕ sits at the end of headers; the toolbar keeps its current
animation; glass on cards is under review. Exceptions are listed here and in PLAN.md, nowhere else.

## 7. Motion

Full rules in [MOTION.md](MOTION.md). In short:
- Springs only, from tokens. Movement and size use spatial springs; opacity, colour and blur use
  effects springs, which never bounce.
- Things morph, never pop; one material per morph; the button becomes the card; closing goes back
  into the source.
- Nothing blocks input; gestures drive a motion value and hand their velocity to the release.
- **Reduce Motion** ("Remove animations"): every Motion piece has a reduced form, agreed with Marco
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
1. All configured required CI checks pass. The workflow runs Spotless, Motion unit tests,
   `:weather-unit:testDebugUnitTest`, `:maps-utils:testDebugUnitTest`, `:app:testBasicDebugUnitTest`,
   and the Basic Debug assembly. The required `No hardcoded UI literals` step also runs in this job;
   it is a heuristic guard on added production Kotlin lines, so manual review remains mandatory;
2. logic has unit tests; each Motion piece has a Motion Lab demo;
3. it was checked on the Pixel (profile build for smoothness; frame stats when motion changed), or is
   clearly marked "not checked on the phone";
4. the docs are updated: PLAN.md (decisions), LOG.md (a dated line), ROADMAP.md (ticked), Motion's
   README for any Motion API change.

## 10. Docs

One source of truth per topic: PLAN.md decisions, LOG.md history, ROADMAP.md to-dos, CONTINUE.md
current handoff, DESIGN/MOTION/WRITING product and design rules, this file Solstice engineering rules,
root AGENTS.md agent operations, and AI_COST_POLICY.md AI/tool/CI efficiency. Shared AI policy is linked,
not copied. Fix contradictions instead of adding to them. Never push upstream's history; never build with `-Pbreezy`.

## 11. Git

1. Work on a branch and merge to `main` through a pull request; nothing is pushed to `main` directly.
2. Commit as INFORMATION89. Never commit `gradlew.bat` (it shows as changed on Linux checkouts),
   secrets, keys or `local.properties`.
3. A pull request says what changed and what was checked (section 9). The `main` branch is protected by the active GitHub ruleset `Protect main`: changes require a pull request and the `build` status check; force pushes and branch deletion are blocked. Keep the repository ruleset aligned with this requirement when workflow check names change.


## 12. AI tools, model selection and cost

AI-assisted development must follow the root [AGENTS.md](../AGENTS.md), this repository's [AI Cost and Resource Efficiency Policy](AI_COST_POLICY.md), and the shared [AI Usage and Cost Policy](https://github.com/INFORMATION89/Motion-UI/blob/main/docs/AI_USAGE_POLICY.md). The local policy adds Android/Gradle-specific guidance; none of these policies override Solstice's product, privacy, design, and approval requirements.

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
