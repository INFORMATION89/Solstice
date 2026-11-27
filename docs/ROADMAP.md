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
- [ ] Validate every active Markdown link and heading reference.
- [ ] Reconcile the current product plan, rules, handoff, and roadmap; keep one authority per subject.
- [ ] Confirm proposal-only material is clearly distinguished from approved decisions.
- [ ] Keep the dated log chronological and historical; do not use it as current implementation guidance.

## Privacy, security, and licensing

- [x] Create an internal, code-backed privacy/data-flow map. It is evidence, not the public notice or legal approval.
- [ ] Complete the audit of location permissions and call paths, providers and request fields, refresh scheduling, app-to-app sharing, diagnostics, retention/deletion, and build variants.
- [ ] Reconcile provider terms, privacy links, attribution, third-party notices, and LGPL-3.0 obligations with the actual shipped build.
- [ ] Review the custom `READ_PROVIDER` permission wording. Treat any user-visible replacement as a separate copy change requiring owner approval.
- [ ] Rewrite the public privacy notice only after the code-path audit is complete and the exact claims have been reviewed and approved.
- [ ] Review Actions permissions, secret exposure, signing, artifact provenance, and release replacement behavior.

## Build, dependencies, and quality

- [x] Restore the consolidated Gradle/CI configuration and diff-based no-hardcoding guard from the reviewed modernization work.
- [ ] Verify the Gradle wrapper, JDK, Android Gradle Plugin, Kotlin, SDK/build-tools, dependency versions, and action pins against the current build.
- [ ] Review the Motion-UI submodule URL and exact pinned commit; do not update the dependency as part of cleanup.
- [ ] Review CI credential scope and permissions; signing secrets must not be available to untrusted pull-request code.
- [ ] Run the required formatting, no-hardcoding, unit-test, and Debug build checks once on the final coherent branch head.
- [ ] Verify the protected-main Profile/signing/publication path only when authorized and configured; do not infer it from PR CI.
- [ ] Record physical-device, TalkBack, visual, and performance checks as NOT TESTED unless actually performed.

## Closure

- [ ] Review the complete diff for unintended app behavior, accidental feature work, duplicate documentation, and stale external dependency tasks.
- [ ] Confirm the final branch changes are limited to cleanup and do not add providers, dependencies, product behavior, or UI features.
- [ ] Open the final pull request with exact-head CI evidence and explicit remaining limitations.
