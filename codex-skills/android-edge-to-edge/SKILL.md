---
name: android-edge-to-edge
description: Use this skill for Android Jetpack Compose edge-to-edge migration, SDK 35 system bar behavior, status bar or navigation bar overlap, IME inset bugs, safeDrawingPadding, Scaffold inset handling, FAB/list padding, or keyboard-obscured input fields.
license: Complete terms in LICENSE.txt
metadata:
  source: https://github.com/android/skills
  upstream-skill: edge-to-edge
---

# Android Edge-to-Edge

Make Compose Android screens edge-to-edge safe without double-applying insets.

## Prerequisites

- The project should use Jetpack Compose.
- If target SDK is below 35, plan the target SDK change before applying SDK 35 edge-to-edge fixes.

## Workflow

1. Inspect every `Activity` touched by the flow. Check target SDK, `enableEdgeToEdge`, `setContent`, manifest `windowSoftInputMode`, theme/system bar configuration, and whether `WindowCompat` is used directly.
2. Add `enableEdgeToEdge()` before `setContent` for Activities that do not already handle it.
3. For screens with soft keyboard input, set `android:windowSoftInputMode="adjustResize"` on the corresponding Activity.
4. Choose one inset strategy per container:
   - Prefer `Scaffold` padding passed into content and consumed.
   - Use Material component inset handling when available.
   - Use `safeDrawingPadding`, `windowInsetsPadding`, or `fitInside(WindowInsetsRulers...)` only where the container is outside a suitable Scaffold.
5. For lists, apply insets through `contentPadding`, not parent `Modifier.padding`, so content can scroll behind bars correctly.
6. For FABs and bottom actions, ensure they sit above navigation bars through Scaffold placement or safe drawing padding.
7. For IME, prefer `fitInside(WindowInsetsRulers.Ime.current)` when available. If using `imePadding`, place it before vertical scroll and avoid combining it with a parent that already includes IME insets.
8. For full-screen dialogs, set `decorFitsSystemWindows = false` when the dialog uses full width/full size.
9. Verify light/dark system bar icon legibility and navigation bar contrast behavior.
10. Build and run the affected screen on a device/emulator when available.

## References

- `references/original-android-skill.md`: upstream detailed patterns and right/wrong snippets.

## Guardrails

- Do not stack multiple inset modifiers that consume the same inset.
- Do not apply parent padding around Material app bars when the app bar should draw behind system bars.
- If using `ComponentActivity.enableEdgeToEdge`, do not duplicate icon color handling that it already manages.
- Treat visual verification as required when fixing overlap bugs.
