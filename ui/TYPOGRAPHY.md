# SehatSaathi Typography, Spacing & Alignment Style Guide

## 1. Font Family (Single Source of Truth)
- **Font Family:** `Manrope` (`R.font.manrope` via `ManropeFontFamily` in `app/src/main/java/com/example/ui/theme/Type.kt`).
- **Rule:** Never use system default `SansSerif`, `Serif`, or `Monospace` for UI copy. All text styles in `SehatTypography` inherit `ManropeFontFamily`.

---

## 2. Type Roles, Sizes & Line-Height Tokens

| Role | Compose Token | Size | Line Height | Ratio | Weight | Line Break / Wrapping |
| :--- | :--- | :---: | :---: | :---: | :---: | :--- |
| **Display / Hero Title (H1)** | `displayLarge` | `40.sp` | `46.sp` | `1.15` | `ExtraBold (800)` | `LineBreak.Heading` (balanced) |
| **Section Hero / Stat Value (H2)** | `displayMedium` | `32.sp` | `38.sp` | `1.19` | `Bold (700)` | `LineBreak.Heading` (balanced) |
| **Page / Screen Heading** | `headlineLarge` | `26.sp` | `33.sp` | `1.27` | `Bold (700)` | `LineBreak.Heading` (balanced) |
| **Section / Card Group Heading (H3)** | `headlineMedium` | `22.sp` | `28.sp` | `1.27` | `SemiBold (600)` | `LineBreak.Heading` (balanced) |
| **Card Title** | `titleLarge` | `20.sp` | `25.sp` | `1.25` | `SemiBold (600)` | `LineBreak.Heading` (balanced) |
| **Row Title / Subheading** | `titleMedium` | `18.sp` | `23.sp` | `1.28` | `SemiBold (600)` | `LineBreak.Heading` (balanced) |
| **Primary Body Copy** | `bodyLarge` | `18.sp` | `28.sp` | `1.56` | `Normal (400)` | `LineBreak.Paragraph` (pretty) |
| **Secondary Body / Input Copy** | `bodyMedium` | `18.sp` | `28.sp` | `1.56` | `Normal (400)` | `LineBreak.Paragraph` (pretty) |
| **Button / Tab / Field Label** | `labelLarge` | `16.sp` | `22.4.sp` | `1.40` | `SemiBold (600)` | Balanced 2-line wrap or `softWrap = false` |
| **Status Chip / Helper / Caption** | `labelMedium` | `14.sp` | `19.6.sp` | `1.40` | `Medium (500)` | `1.40` ratio, never below `14.sp` |
| **Compact Badge / Timestamp** | `labelSmall` | `14.sp` | `19.6.sp` | `1.40` | `Medium (500)` | `1.40` ratio, never below `14.sp` |

### Hard Minimums
- **Body text:** Always `>= 18.sp` (`bodyLarge` and `bodyMedium` are both `18.sp`).
- **Captions, badges & helper text:** Always `>= 14.sp` (`labelMedium` and `labelSmall` are both `14.sp`).
- **Tabular Numerals:** Apply `.tabular()` (`fontFeatureSettings = "tnum"`) to all glucose/BP readings, dose counts, timers, streaks, percentages, and dates.

---

## 3. Spacing Scale & Vertical Rhythm
- **8dp Base Grid:** Use spacing tokens from `{ 4.dp, 8.dp, 12.dp, 16.dp, 20.dp, 24.dp, 32.dp, 48.dp }`.
- **Card Padding:** Default `PaddingValues(20.dp)` (`SehatCard`).
- **Button Padding:** `PaddingValues(horizontal = 20.dp, vertical = 12.dp)` on `SehatPrimaryButton`, `SehatSecondaryButton`, and `SehatDestructiveButton`.
- **Touch Targets:** Minimum `48.dp x 48.dp` on all interactive controls (`56.dp` minimum height on primary touch actions and form inputs).

---

## 4. Alignment & Wrapping Rules
1. **Left-Align Multi-Line Copy:** All body paragraphs, helper notes, medical guidance steps, and form labels use `TextAlign.Start`. Center alignment (`TextAlign.Center`) is restricted to single-line button labels, gauge readouts, and stat cells.
2. **Never Split Numbers from Units:** Use `nonBreakingUnit(value, unit)` (e.g., `"148\u00A0mg/dL"`) or `BaselineValueWithUnit(value = "148", unit = "mg/dL")` with `Modifier.alignByBaseline()` so clinical values and units (`mg/dL`, `mmHg`, `mg`, `days`) never wrap onto separate lines.
3. **Zero Truncation on Clinical & Action Copy:** Never truncate medicine names, doses, readings, emergency contacts, or safety warnings with `maxLines = 1`. Buttons allow up to 2 balanced lines (`maxLines = 2`), and 2-button action rows stack vertically when `maxWidth < 360.dp`.
4. **Reserved Supporting Space in Forms:** `SehatTextField` aligns label, input, and helper/error text on a shared left edge and supports `reserveSupportingSpace = true` (`minHeight = 24.dp`) so validation errors do not shift layout below the field.
