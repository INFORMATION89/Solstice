# Solstice cleanup roadmap

**Scope:** pre-feature cleanup of the Solstice Android application.  
**Status:** active. No new product features, visual redesigns, providers, or runtime behavior changes are included.

The previous backlog is preserved in [archive/ROADMAP_before_cleanup_2026-11-27.md](archive/ROADMAP_before_cleanup_2026-11-27.md).
It contains historical proposals and mixed dependency work; do not resume an item until it has been
revalidated against current source, current product decisions, and the dependency boundaries in
[INTEGRATIONS.md](INTEGRATIONS.md).

## Documentation and governance

- [x] Restore the Solstice agent entry point, AI cost policy, modernization checklist, privacy evidence map, and consumer-side integration boundary.
- [x] Remove duplicated Meridian and Motion-UI implementation plans/specifications from the active Solstice documentation tree.
- [x] Remove stale references to deleted dependency documents from the current plan and engineering rules.
- [x] Validate every active Markdown link and heading reference (52 Markdown files; internal paths and coverage anchors checked).
- [x] Reconcile the current product plan, rules, handoff, and roadmap; keep one authority per subject.
- [x] Confirm proposal-only material is clearly distinguished from approved decisions; decided markers are historical and PLAN.md is authoritative.
- [x] Keep the dated log chronological and historical; 81 dated entries are in order.

## Privacy, security, and licensing

- [x] Create an internal, code-backed privacy/data-flow map. It is evidence, not the public notice or legal approval.
- [ ] Complete the remaining audit of location permissions and call paths, provider request fields and retention, refresh scheduling, diagnostics, local storage/backup/deletion, and build variants.
- [ ] Reconcile provider terms, privacy links, attribution, third-party notices, and LGPL-3.0 obligations with the actual shipped build.
- [ ] Review the custom `READ_PROVIDER` permission wording. Treat any user-visible replacement as a separate copy change requiring owner approval.
- [x] Replace inherited Breezy wording with a Solstice-owned notice grounded in the current data map; keep unresolved technical and legal review items open.
- [x] Review Actions permissions, secret exposure, signing, artifact provenance, and release replacement behavior at workflow-configuration level; secret values and main-only publication remain unverified.

## Build, dependencies, and quality

- [x] Restore the consolidated Gradle/CI configuration and diff-based no-hardcoding guard from the reviewed modernization work.
- [x] Verify the declared Gradle wrapper, JDK, Android Gradle Plugin, Kotlin, SDK/build-tools resolution, and workflow action pins against the successful CI build; no dependency refresh was performed.
- [x] Review the Motion-UI submodule URL and exact pinned commit; the pinned commit exists and was built by CI. No dependency update was made.
- [x] Review CI credential scope and permissions; signing secrets are not exposed to pull-request builds.
- [ ] Run the required formatting, no-hardcoding, unit-test, and Debug build checks once on the final coherent branch head.
- [ ] Verify the protected-main Profile/signing/publication path only when authorized and configured; do not infer it from PR CI.
- [x] Record physical-device, TalkBack, visual, and performance checks as NOT TESTED; see CONTINUE.md.

**Verification note:** Build run 46050051016 succeeded on commit `8dc4131234594c54897a42a7909f38cd8ba64d19`. The final branch includes documentation-only follow-ups and a comment-only Gradle settings correction; exact-head CI must still run after PR #22 is reopened. GitHub currently lists no published releases.

## Closure

- [x] Review the complete diff for unintended app behavior, accidental feature work, duplicate documentation, and stale external dependency tasks.
- [x] Confirm the final branch changes are limited to cleanup and do not add providers, dependencies, product behavior, or UI features.
- [ ] Open the final pull request with exact-head CI evidence and explicit remaining limitations.
