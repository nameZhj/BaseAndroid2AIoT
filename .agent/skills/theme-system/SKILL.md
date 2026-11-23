# Theme System Skill

Load this skill when changing design tokens, theme persistence, Material 3 color schemes, or shared UI components.

## Palette
- Default accent: Arctic Industrial.
- Optional accent: Electric Blue.
- Persist accent selection through `ThemeManager.setThemeAccent()`; observe `themeAccentFlow` at the app theme boundary.
- Keep Light and Dark palettes separately tuned. Do not assume the same foreground color works on both backgrounds.
- Feature code consumes `AppTheme.colors`; palette values must not be copied into feature screens.

## Semantic colors
- `accentPrimary` is the active accent, not a generic status color.
- Use `accentGreen`, `accentAmber`, and `accentRed` for success, warning, and danger semantics.
- Always pair accent surfaces with a foreground that passes contrast checks.
- Map primary, secondary, tertiary, error, surface, outline, and on-colors into Material 3 `ColorScheme`.

## Components and accessibility
- Shared buttons keep at least a 48dp minimum height and a clear pressed/disabled state.
- Center button labels; use sufficient horizontal padding and avoid truncating important action labels.
- Check foreground/background contrast in both themes. Target WCAG AA: 4.5:1 for normal text and 3:1 for large text.
- Do not encode state using color alone; include text, iconography, or shape where appropriate.
- Decorative icons use a null content description; functional icons require localized descriptions.

## Verification
- Check Arctic and Electric Blue in both Light and Dark modes.
- Check button label contrast against the actual button container, not the app background.
- Check selected, disabled, warning, error, and focus states.