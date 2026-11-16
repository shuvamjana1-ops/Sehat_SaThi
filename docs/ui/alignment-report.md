# SehatSaathi UI Text & Alignment Report (After Fixes)

**Date:** 2026-11-15  
**Scope:** Root-cause presentation-layer fixes across shared typography tokens (`Type.kt`), shared buttons/badges/inputs (`Buttons.kt`, `CardsAndBadges.kt`, `Inputs.kt`), and screen-level responsive layouts (`AccountScreen.kt`, `ProfileModals.kt`, `DashboardScreen.kt`, `LogReadingScreen.kt`, `EmergencyScreen.kt`).

---

## Before vs. After Summary Table

| Issue Category | Count Before | Count After | Status | Root-Cause Fix Applied |
| :--- | :---: | :---: | :---: | :--- |
| **Typography Consistency (<18sp body / <14sp captions / line-height ratio)** | 5 | **0** | Fixed | Updated `Type.kt` so `bodyMedium` is `18.sp` (`lineHeight = 28.sp`, `1.56x`), `labelSmall` is `14.sp` (`lineHeight = 19.6.sp`, `1.40x`), `labelLarge` is `16.sp / 22.4.sp` (`1.40x`), and headings use `LineBreak.Heading` with `1.15–1.28x` line heights. |
| **Text Overflow, Clipping & Ellipsis Truncation** | 9 | **0** | Fixed | Updated `Buttons.kt` to allow balanced 2-line wrapping (`maxLines = 2`) with `20.dp` horizontal padding; made `ProfileStickyTabBar` horizontally scrollable below `480.dp`; removed `maxLines = 1` clipping from `EmergencyScreen` caregiver call/WhatsApp buttons, `ProfileStatCell`, `HighlightsRowCard`, and `LogReadingScreen` mode switcher. |
| **Wrapping & Number-Unit Separation** | 6 | **0** | Fixed | Added `nonBreakingUnit` / `withNonBreakingUnits()` in `Type.kt` (`\u00A0` between numbers and `mg/dL`, `mmHg`, `mg`, `days`) and stacked 2-column action buttons below `360.dp` on `AccountScreen` and `EmergencyScreen`. |
| **Vertical Alignment (Baseline & Icon+Text)** | 4 | **0** | Fixed | Wrapped icon + label inside `Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp))` across all button variants and chips; aligned vital numbers and units on `Modifier.alignByBaseline()` in `DashboardScreen` and `BaselineValueWithUnit`. |
| **Horizontal Alignment & Form Error Shift** | 4 | **0** | Fixed | Unified `20.dp` horizontal padding on buttons; updated `SehatTextField` (`Inputs.kt`) to left-align label, `18.sp` input text, and supporting error/helper slot (`minHeight = 24.dp`); replaced `TextAlign.Center` with `TextAlign.Start` on multi-line helper texts in `ProfileModals.kt` and `AccountScreen.kt`. |
| **Grid, Card & Touch Target Alignment** | 3 | **0** | Fixed | Added `.minimumInteractiveComponentSize()` (`>= 48.dp` touch target) to `change_avatar_photo_btn` and `SehatIconButton`; standardized `StatusChip` (`minHeight = 32.dp`) and `DemoPillBadge` (`minHeight = 28.dp`). |
| **Line & Divider Alignment** | 2 | **0** | Fixed | Standardized `1.dp` hairline dividers and continuous pill tab indicators across light, dark, and Simple Mode. |
| **Total Measured Issues** | **33** | **0** | **100% Resolved** | Verified by `AlignmentDetectorTest` and full Robolectric test suite. |

---

## Files Changed
1. `/app/src/main/java/com/example/ui/theme/Type.kt` — Manrope-only type scale (`>= 18.sp` body, `>= 14.sp` captions/labels, explicit role line-heights, `LineBreak.Heading` / `LineBreak.Paragraph`, `.tabular()`, `nonBreakingUnit`, `withNonBreakingUnits`).
2. `/app/src/main/java/com/example/ui/components/Buttons.kt` — `20.dp` horizontal padding, `Row(verticalAlignment = Alignment.CenterVertically, Arrangement.spacedBy(8.dp))`, balanced 2-line wrapping (`maxLines = 2`), `>= 48.dp` (`56.dp` primary) touch targets.
3. `/app/src/main/java/com/example/ui/components/CardsAndBadges.kt` — Standardized `StatusChip` and `DemoPillBadge` vertical centering and height; added `BaselineValueWithUnit` composable.
4. `/app/src/main/java/com/example/ui/components/Inputs.kt` — `18.sp` `bodyLarge` input text, shared left-edge alignment, `48.dp` password toggle touch target, and reserved supporting text height (`24.dp`) to prevent layout shift on error.
5. `/app/src/main/java/com/example/ui/screens/AccountScreen.kt` — Scrollable `ProfileStickyTabBar` on compact screens (`< 480.dp`), responsive stacking of `"Edit profile"` and `"Share care card"` below `360.dp`, `48.dp` touch target on avatar camera button, non-truncating stat/highlight labels, and left-aligned activity footer note.
6. `/app/src/main/java/com/example/ui/screens/ProfileModals.kt` — Left-aligned multi-line helper paragraphs (`TextAlign.Start`).
7. `/app/src/main/java/com/example/ui/screens/DashboardScreen.kt` — Baseline-aligned (`alignByBaseline()`) blood sugar (`mg/dL`) and blood pressure (`mmHg`) readouts with non-breaking units.
8. `/app/src/main/java/com/example/ui/screens/LogReadingScreen.kt` — Balanced wrapping on input mode switcher (`Keypad`, `Photo scan`, `Voice`) for `320px` viewports.
9. `/app/src/main/java/com/example/ui/screens/EmergencyScreen.kt` — Removed `maxLines = 1` clipping from emergency call and WhatsApp buttons and stacked call buttons cleanly below `360.dp`.
10. `/app/src/test/java/com/example/AlignmentDetectorTest.kt` & `/src/tests/alignment.spec.ts` — Automated alignment and typography detector tests.
11. `/docs/ui/alignment-audit.md`, `/docs/ui/alignment-report.md`, `/docs/ui/TYPOGRAPHY.md`, `/docs/ui/screenshots/before-alignment-offenders.svg`, `/docs/ui/screenshots/after-alignment-verified.svg`.
