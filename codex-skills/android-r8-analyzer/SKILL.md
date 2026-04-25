---
name: android-r8-analyzer
description: Use this skill to analyze Android R8, ProGuard, minify, shrinker, and keep-rule configuration, including redundant keep rules, reflection keep rules, broad package-wide rules, app size optimization, or library consumer rules. This skill analyzes and recommends; it should not edit keep-rule files unless separately requested.
license: Complete terms in LICENSE.txt
metadata:
  source: https://github.com/android/skills
  upstream-skill: r8-analyzer
---

# Android R8 Analyzer

Analyze R8/ProGuard configuration and produce actionable keep-rule recommendations without changing rules by default.

## Workflow

1. Inspect Gradle files, `gradle.properties`, minify settings, consumer rules, and ProGuard/R8 rule files. Use `references/CONFIGURATION.md`.
2. If AGP is below 9, mention that AGP 9 includes app-optimization improvements, but keep the analysis focused on the current project.
3. Evaluate keep rules in this order:
   - Library rules first, using `references/REDUNDANT-RULES.md`.
   - Remaining rules by impact, using `references/KEEP-RULES-IMPACT-HIERARCHY.md`.
   - Subsuming/broader rules that make narrower rules redundant.
   - Reflection-driven rules, using `references/REFLECTION-GUIDE.md`.
4. For every rule worth reporting, state the action: remove, narrow, keep, or investigate further. Include the local file path and reason.
5. Do not report empty categories.
6. If the user asked only for analysis, do not edit keep-rule files. If they ask for fixes afterward, make a separate targeted change and run verification.
7. Recommend focused runtime/UI coverage for packages affected by keep-rule changes, especially reflection-heavy paths.

## References

- `references/original-android-skill.md`: upstream full checklist.
- `references/CONFIGURATION.md`: R8 configuration audit.
- `references/REDUNDANT-RULES.md`: library keep rules often covered by consumer rules.
- `references/KEEP-RULES-IMPACT-HIERARCHY.md`: broadness/subsumption analysis.
- `references/REFLECTION-GUIDE.md`: narrowing rules for reflection.

## Guardrails

- Do not mention internal skill files in the user-facing findings unless the user asks how the skill works.
- Do not claim size wins without measurement.
- Do not call out impact "levels" as labels; translate them into practical risk and action.
- Do not hide analysis errors; summarize blockers plainly if a file cannot be inspected.
