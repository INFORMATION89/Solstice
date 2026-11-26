# Solstice — Claude Code entry point

Solstice is an Android weather application and a fork of Breezy Weather. Its product identity, weather behavior, privacy requirements, and roadmap remain owned by this repository.

**Read the root [AGENTS.md](AGENTS.md) first.** It is the operational entry point for all coding agents, including Claude Code. Then follow its reading order:

1. [docs/RULES.md](docs/RULES.md) for Solstice-specific engineering, product, design, privacy, clean-room, and approval constraints.
2. [docs/PLAN.md](docs/PLAN.md) for decisions in force; [docs/ROADMAP.md](docs/ROADMAP.md) for remaining work.
3. [docs/CONTINUE.md](docs/CONTINUE.md) for the current handoff and next bounded task.
4. [docs/AI_COST_POLICY.md](docs/AI_COST_POLICY.md) and the shared [Motion-UI AI Usage and Cost Policy](https://github.com/INFORMATION89/Motion-UI/blob/main/docs/AI_USAGE_POLICY.md) for model selection, Claude/ChatGPT coordination, context efficiency, spending controls, and verification costs.
5. [docs/MODERNIZATION_CHECKLIST.md](docs/MODERNIZATION_CHECKLIST.md) before continuing the documentation, CI, privacy, or cross-repository audit.

Read specialist references only as required by the task: [DESIGN.md](docs/DESIGN.md), [MOTION.md](docs/MOTION.md), [WRITING.md](docs/WRITING.md), [UI_SYSTEMS.md](docs/UI_SYSTEMS.md), [SOURCES.md](docs/SOURCES.md), [MOTION_ADOPTION_PLAN.md](docs/MOTION_ADOPTION_PLAN.md), or scoped specifications/research.

The local Meridian documents `ENGINE_SPEC.md` and `MERIDIAN_PLAN.md` are historical Solstice-side research references. They are not the current authority for Meridian's language, storage, API, packaging, or milestones. Follow the current [Meridian-Engine master plan](https://github.com/INFORMATION89/Meridian-Engine/blob/main/PLAN.md).

Motion is an independent Android/Jetpack Compose library, currently consumed as the `motion/` submodule. The separate `:motion-ui` layer is still planned, not an available dependency. Verify exact merged commits and CI before adopting APIs or updating the submodule.

Claude-specific instructions do not override `AGENTS.md`, `docs/RULES.md`, or `docs/AI_COST_POLICY.md`. Never push upstream Git history into this repository; never build with `-Pbreezy`.
