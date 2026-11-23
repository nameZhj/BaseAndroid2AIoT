# Adaptive Layout Skill

Load this skill when changing responsive Compose layouts, Pad navigation, dialogs, or window-size behavior.

## Source of truth
- Base decisions on the current app window, not physical device identity, diagonal size, or orientation alone.
- Use Compact < 600dp, Medium 600-839dp, Expanded >= 840dp as the default width breakpoints.
- `AdaptiveContentLayout` exposes the width class and pane recommendation.
- `rememberPadSpec()` supplies window-sensitive control dimensions; `rememberPadDeviceClass()` classifies the current window.
- `PadMasterDetailLayout` only shows both panes when its measured width is at least 840dp.

## Layout rules
- Compact windows use one primary pane and navigation that fits the available width.
- Medium windows may use a compact master-detail pattern only when both panes remain usable; otherwise use one pane.
- Expanded windows may use two panes. Give each pane a meaningful minimum width and account for fixed dividers and spacing.
- Do not use landscape orientation as a shortcut for enabling two panes.
- Re-evaluate after rotation, split-screen resizing, freeform resizing, and fold/unfold transitions.
- Use `BoxWithConstraints` when a component needs its own measured constraints.
- Preserve 48dp minimum interactive targets; allow text to wrap or ellipsize without shrinking adjacent controls.
- Dialogs and sheets should have bounded widths on large windows and scrollable content when height is constrained.

## Verification
- Preview or test widths around 360, 600, 839, 840, 1200, and 1600dp.
- Test both portrait and landscape, plus a narrow split-screen window.
- Verify no controls overlap, no action is clipped, and the selected pane/navigation state survives resizing.