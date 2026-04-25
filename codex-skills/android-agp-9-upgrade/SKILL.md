---
name: android-agp-9-upgrade
description: Use this skill when upgrading an Android project to Android Gradle Plugin 9, including AGP 9 migration, Gradle DSL migration, built-in Kotlin migration, KSP/kapt migration, BuildConfig changes, or AGP 9 compatibility checks. Do not use for Kotlin Multiplatform projects.
license: Complete terms in LICENSE.txt
metadata:
  source: https://github.com/android/skills
  upstream-skill: agp-9-upgrade
---

# Android AGP 9 Upgrade

Migrate a non-Kotlin-Multiplatform Android project to Android Gradle Plugin 9 with focused Gradle changes and verification.

## Workflow

1. Confirm this is not a Kotlin Multiplatform project. If it is KMP, stop and explain that this skill does not apply.
2. Inspect AGP, Gradle, JDK, Kotlin, KSP, Hilt, kapt, Paparazzi, and version catalog usage.
3. If the project is below AGP 9 and the user did not explicitly ask Codex to perform the full upgrade, ask them to run Android Studio AGP Upgrade Assistant first. If they did ask Codex to do it, verify the latest stable compatibility data from official Android docs before editing.
4. Read `references/android/build/releases/agp-9-0-0-release-notes.md` for breaking changes and compatibility.
5. Update dependencies that have AGP 9 minimums. Pay special attention to KSP and Hilt.
6. Migrate to built-in Kotlin using `references/android/build/migrate-to-built-in-kotlin.md`.
7. Migrate old Android Gradle DSL usage using the release notes and `references/recipes.md`.
8. Handle kapt/KSP changes with `references/ksp-kapt.md`.
9. Check custom `BuildConfig` fields with `references/buildconfig.md`.
10. Clean up obsolete `gradle.properties` flags after migration.
11. Verify with Gradle commands appropriate to the repo. Prefer `./gradlew help` and `./gradlew build --dry-run`; do not run `clean` unless the user asks or a stale-build issue is being diagnosed.

## Troubleshooting References

- `references/paparazzi-gradle-9.md`: Paparazzi compatibility issues.
- `references/original-android-skill.md`: upstream full workflow.

## Guardrails

- Keep Gradle edits scoped and mechanical; do not restructure modules.
- Preserve version catalog style if the project uses one.
- Do not add `android.disallowKotlinSourceSets=false`.
- Do not use Python scripts for the migration.
