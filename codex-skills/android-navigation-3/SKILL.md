---
name: android-navigation-3
description: Use this skill when migrating to or implementing Android Jetpack Navigation 3, including Navigation 2 migration, NavKey, NavDisplay, type-safe navigation, deep links, multiple back stacks, adaptive scenes, Hilt ViewModel navigation, or returning navigation results.
license: Complete terms in LICENSE.txt
metadata:
  source: https://github.com/android/skills
  upstream-skill: navigation-3
---

# Android Navigation 3

Use this for Navigation 2 to Navigation 3 migrations and for adding Navigation 3 patterns to Compose Android apps.

## Workflow

1. Determine the current navigation stack: Navigation 2 XML, Navigation Compose, custom routing, or mixed.
2. Read `references/android/guide/navigation/navigation-3/migration-guide.md` before migration work. For string route Compose projects, also read `references/android/guide/navigation/type-safe-destinations.md`.
3. Update dependencies in the same version-management style the project already uses.
4. Model destinations as typed navigation keys and move string-route assumptions out of call sites.
5. Build a stable back stack state model, using saveable state where process death or configuration change recovery is expected.
6. Implement entries with the project's DI, ViewModel, and module boundaries. Keep feature modules decoupled; expose destination keys or entry providers rather than leaking screen internals.
7. Pick only the recipes needed for the user's case: common UI, deep links, scenes, adaptive layouts, multiple back stacks, conditional auth/onboarding flows, result passing, or animations.
8. Verify with compile/tests and at least one navigation smoke path for each changed flow.

## Recipe Map

- Basics: `references/android/guide/navigation/navigation-3/recipes/basic.md`, `basicsaveable.md`, `basicdsl.md`.
- Common UI and multiple stacks: `common-ui.md`, `multiple-backstacks.md`.
- Deep links: `deeplinks-basic.md`, `deeplinks-advanced.md`.
- Scenes and adaptive UI: `dialog.md`, `bottomsheet.md`, `scenes-listdetail.md`, `scenes-twopane.md`, `material-listdetail.md`, `material-supportingpane.md`.
- Architecture: `modular-hilt.md`, `modular-koin.md`, `passingarguments.md`.
- Results: `results-event.md`, `results-state.md`.

## Guardrails

- Avoid half-migrated duplicate navigation sources of truth unless explicitly doing an incremental bridge.
- Preserve existing deep link behavior and back stack expectations.
- Do not flatten feature-module boundaries for convenience.
- If behavior depends on a recently changed Navigation 3 API, verify against official Android documentation before implementing.
