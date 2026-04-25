---
name: android-compose-xml-migration
description: Use this skill when migrating an Android XML View or layout to Jetpack Compose, including XML layout conversion, ComposeView interop, theme setup, screenshot parity, or replacing legacy Android Views incrementally.
license: Complete terms in LICENSE.txt
metadata:
  source: https://github.com/android/skills
  upstream-skill: migrate-xml-views-to-jetpack-compose
---

# Android Compose XML Migration

Migrate one XML View or layout at a time into Jetpack Compose while preserving behavior, styling, and visual parity.

## Workflow

1. Identify the target XML layout. If the user did not name one, read `references/identify-optimal-xml-candidate.md` and inspect the project for a low-risk candidate.
2. Analyze the layout, resources, theme usage, adapters, custom views, data binding, and call sites. Use `references/analysis-of-the-project-and-layout.md` for the audit checklist.
3. Write a short migration plan before editing. Keep scope to one screen/component unless the user explicitly asks for a broader migration.
4. Capture a visual baseline when practical: existing screenshot test, emulator screenshot, or a user-provided image. If unavailable, continue with source-based parity checks and note the gap.
5. Ensure Compose dependencies, compiler, and minimal theme bridging exist. Read the setup references only when the project is missing that part.
6. Implement the new composable using existing project patterns. Add a Compose Preview for the migrated UI.
7. Replace usages through the narrowest interoperability path:
   - XML/View host to Compose: `ComposeView`.
   - Compose host needing existing Views: `AndroidView`.
8. Validate with compile/tests and, when possible, screenshot comparison. Only delete XML/resources after confirming no remaining references.

## Reference Selection

- `references/original-android-skill.md`: upstream full workflow.
- `references/xml-layout-migration.md`: XML-to-Compose mapping rules.
- `references/android/develop/ui/compose/setup-compose-dependencies-and-compiler.md`: dependency/compiler setup.
- `references/android/develop/ui/compose/designsystems/migrate-xml-theme-to-compose.md`: minimal theme migration.
- `references/android/develop/ui/compose/migrate/interoperability-apis/compose-in-views.md`: Compose inside Views.
- `references/android/develop/ui/compose/migrate/interoperability-apis/views-in-compose.md`: Views inside Compose.

## Guardrails

- Do not rewrite unrelated screens or global themes unless required for the chosen layout.
- Preserve names, resources, colors, dimensions, text behavior, accessibility labels, and existing state ownership.
- Prefer project-local UI patterns over generic Compose examples.
- If deleting XML, search all source/resource references first.
