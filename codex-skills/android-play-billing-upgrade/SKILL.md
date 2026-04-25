---
name: android-play-billing-upgrade
description: Use this skill when upgrading Google Play Billing Library in an Android project, including Play Billing migration, billing library version compliance, BillingClient API changes, subscriptions migration, deprecated SkuDetails/ProductDetails changes, or purchase flow updates.
license: Complete terms in LICENSE.txt
metadata:
  source: https://github.com/android/skills
  upstream-skill: play-billing-library-version-upgrade
---

# Android Play Billing Upgrade

Upgrade Google Play Billing Library by detecting the effective API baseline, choosing a direct or stepped path, and refactoring by behavior rather than search-and-replace.

## Workflow

1. Tell the user you are upgrading Play Billing Library.
2. Locate billing dependencies in Gradle files or version catalogs. Then build/compile if practical to capture the starting failure mode.
3. Determine the effective version. If code uses deprecated APIs that imply an older baseline than the declared dependency, treat the code baseline as the effective version.
4. Verify the latest stable target and release notes from official Android/Google Play documentation before editing.
5. Plan the path:
   - Direct migration if the effective version is within two major versions of the target.
   - Stepped migration if it is more than two major versions behind.
6. For every major jump, consult the relevant migration guide, `references/android/google/play/billing/release-notes.md`, `references/migration-logic.md`, and `references/version-checklist.md`.
7. Update SDK/dependency requirements first, preserving the project's dependency-management style.
8. Refactor by intent: product querying, purchase flow, acknowledgements, subscriptions, pending purchases, connection/retry behavior, and feature-specific logic.
9. For stepped migrations, compile after each intermediate major version.
10. Final verification should include unit tests, implementation tests where present, and an Android build command suitable for the repo.
11. Explain what changed and why, especially SDK requirements, removed deprecated APIs, and new library behavior replacing custom logic.

## Guardrails

- Do not rely only on dependency version strings; source APIs can reveal the true migration baseline.
- Do not perform blind class renames.
- Treat billing behavior as high risk: preserve purchase acknowledgement, entitlement, retry, and error handling semantics.
- If latest version information is needed, browse official docs rather than relying on model memory.
