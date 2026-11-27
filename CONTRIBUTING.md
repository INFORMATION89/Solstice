# Contributing to Solstice

Solstice is an actively redesigned Android weather application based on a Breezy Weather fork. The repository still contains inherited upstream code and licence obligations; read the root `LICENSE` and preserve applicable copyright and attribution notices.

## Before starting work

- For a new feature, visible behavior, screen, setting, data flow, or design change, open an issue or discuss the proposal with the maintainer first. Solstice's [project rules](docs/RULES.md) require approval before new product/design behavior is built.
- For a bug fix, describe the observed behavior, expected behavior, reproduction steps, and affected Android/API version where known.
- Check [PLAN.md](docs/PLAN.md), [ROADMAP.md](docs/ROADMAP.md), and [CONTINUE.md](docs/CONTINUE.md) before selecting work. The roadmap may contain proposals that are not approved for implementation.
- Do not assume that a Breezy Weather contribution guide, issue label, maintainer workflow, F-Droid channel, or upstream repository process applies to Solstice.

## Development workflow

1. Fork the repository and create a focused branch from the current `main`.
2. Keep the change limited to one coherent issue or task. Avoid unrelated formatting, broad refactors, or changes to product behavior not covered by the agreed scope.
3. Follow the root [AGENTS.md](AGENTS.md), [docs/RULES.md](docs/RULES.md), and [docs/AI_COST_POLICY.md](docs/AI_COST_POLICY.md).
4. Use the committed Gradle Wrapper and the JDK version in `.github/.java-version`. Do not rely on an arbitrary system Gradle installation.
5. Run the smallest relevant tests while iterating, then the required CI checks for the completed change. Record checks not run, including physical-device validation.
6. Open a pull request against `main`. Include the issue/proposal, summary, scope, relevant screenshots for UI changes, tests/CI results, and known limitations.

Do not push directly to `main`, force-push shared branches, or merge your own pull request unless the repository owner explicitly authorizes it.

## AI-assisted contributions

AI tools may assist with research, implementation, testing, and documentation. Contributors remain responsible for every submitted line and every factual claim.

- Follow `docs/AI_COST_POLICY.md` and the shared [AI Usage and Cost Policy](https://github.com/INFORMATION89/Motion-UI/blob/main/docs/AI_USAGE_POLICY.md).
- Use one primary agent per coherent task. Disclose which AI tools materially contributed to the pull request and what they were used for.
- Do not paste secrets, signing credentials, private keys, personal data, or production user data into prompts, issues, logs, or examples.
- Do not introduce paid APIs, subscriptions, credits, hosted services, or ongoing costs without the owner's explicit approval.
- For difficult architecture, security, privacy, licensing, or cross-repository changes, an independent **Opus review recommended** may be recorded in the task brief. This is optional and does not replace tests or maintainer approval.
- Follow the clean-room rules in the repository that owns the work. Motion and Meridian are independent projects with their own source/research requirements; Solstice must not copy their implementation code or make them depend on Solstice.

## Pull request checklist

- [ ] The change matches an agreed issue, task, or documented decision.
- [ ] The diff contains no secrets, `local.properties`, signing material, generated local files, or accidental `gradlew.bat` changes.
- [ ] New or changed logic has relevant tests.
- [ ] User-visible strings are resources and use the approved Solstice wording/translation workflow.
- [ ] Accessibility, reduced-motion, offline, loading, error, and empty states were considered where relevant.
- [ ] Documentation, roadmap, and dated log are updated where their truth changed.
- [ ] CI results are reported against the exact PR head SHA; device-only checks are clearly marked `NOT TESTED` or `DEFERRED` when not run.
- [ ] Third-party dependencies, source consultations, licences, and attribution requirements are disclosed.

The maintainer may request changes or close a pull request that conflicts with current project direction, privacy/security constraints, licensing, or the agreed scope.
