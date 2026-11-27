# Solstice — Claude Code entry point

Solstice is an Android weather application based on a Breezy Weather fork. Its product identity,
weather behavior, privacy requirements, UX, and roadmap are owned by this repository.

**Read root [AGENTS.md](AGENTS.md) first.** It is the operational entry point for all coding agents,
including Claude Code. Then follow its reading order:

1. [docs/RULES.md](docs/RULES.md) for Solstice product, engineering, privacy, licensing, and approval rules.
2. [docs/PLAN.md](docs/PLAN.md) for decisions in force; [docs/ROADMAP.md](docs/ROADMAP.md) for app-owned work.
3. [docs/CONTINUE.md](docs/CONTINUE.md) for the current app handoff.
4. [docs/AI_COST_POLICY.md](docs/AI_COST_POLICY.md) and the shared
   [AI Usage and Cost Policy](https://github.com/INFORMATION89/Motion-UI/blob/main/docs/AI_USAGE_POLICY.md)
   for model selection, context efficiency, spending controls, and verification costs.
5. [docs/MODERNIZATION_CHECKLIST.md](docs/MODERNIZATION_CHECKLIST.md) before continuing documentation,
   CI, privacy, or app governance cleanup.

Read specialist references only as needed: [DESIGN.md](docs/DESIGN.md), [MOTION.md](docs/MOTION.md),
[WRITING.md](docs/WRITING.md), [UI_SYSTEMS.md](docs/UI_SYSTEMS.md), [PROPOSALS.md](docs/PROPOSALS.md),
[INTEGRATIONS.md](docs/INTEGRATIONS.md), or a Solstice-owned feature specification.

External libraries and services are independently owned. Their implementation plans, internal
milestones, labs, and technical decisions belong in their own repositories. Solstice's consumer-side
boundary is documented only in [INTEGRATIONS.md](docs/INTEGRATIONS.md).

Claude-specific instructions do not override [AGENTS.md](AGENTS.md), [RULES.md](docs/RULES.md), or
[AI_COST_POLICY.md](docs/AI_COST_POLICY.md). Never push upstream Git history into this repository; never
build with `-Pbreezy`.
