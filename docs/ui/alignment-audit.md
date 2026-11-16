# SehatSaathi UI Text & Alignment Audit (Before Fixes)

**Date:** 2026-11-15  
**Scope:** Full presentation-layer audit across all routes, modals, sheets, menus, toasts, empty/loading/error states, Dark Mode, and Simple Mode.  
**Viewports Tested:** `320x568`, `360x800`, `390x844`, `768x1024`, `1024x768`, `1440x900`, `800x360` (landscape phone).

---

## Executive Summary (Before Fixes)

| Issue Category | Count Before | Severity | Root Cause Summary |
| :--- | :---: | :---: | :--- |
| **Typography Consistency (<18sp body / <14sp captions / line-height ratio)** | 5 | High | `Type.kt` defines `bodyMedium` at `16.sp` (< 18sp requirement) and `labelSmall` at `13.sp` (< 14sp requirement); `titleLarge`/`titleMedium` line-heights slightly outside role target ratios. |
| **Text Overflow, Clipping & Ellipsis Truncation** | 9 | High | `SehatPrimaryButton`, `SehatSecondaryButton`, `SehatGhostButton`, and `SehatDestructiveButton` force `maxLines = 1, overflow = TextOverflow.Ellipsis`; `ProfileStickyTabBar` squeezes 5 tabs into `320dp` without horizontal scroll; `EmergencyScreen` truncates caregiver names with `maxLines = 1`. |
| **Wrapping & Number-Unit Separation** | 6 | Medium | Vital readings (`148 mg/dL`, `128/82 mmHg`, `62 mg/dL`) use regular breaking spaces in several cards/summaries instead of non-breaking spaces (`\u00A0`) or baseline-aligned sibling pairs; `AccountScreen` action buttons (`Edit profile` + `Share care card`) crowd at `320dp`. |
| **Vertical Alignment (Baseline & Icon+Text)** | 4 | Medium | `SehatSecondaryButton`, `SehatGhostButton`, and `SehatDestructiveButton` do not wrap icon + text in an explicit `Row(verticalAlignment = Alignment.CenterVertically)` with `20.dp` icon sizing; large vital numbers and unit labels lack `alignByBaseline()`. |
| **Horizontal Alignment & Form Error Shift** | 4 | Medium | `SehatSecondaryButton` uses `18.dp` horizontal padding instead of `20.dp`; `SehatTextField` conditionally mounts helper/error text causing a `20.dp` vertical layout shift when validation triggers; multi-line empty/footer notes use `TextAlign.Center` instead of left alignment. |
| **Grid, Card & Touch Target Alignment** | 3 | Medium | `change_avatar_photo_btn` on `AccountScreen` is `34.dp` without `.minimumInteractiveComponentSize()` (< `48.dp` minimum); `StatusChip` and `DemoPillBadge` lack explicit `defaultMinSize(minHeight = 32.dp)` for uniform chip row heights. |
| **Line & Divider Alignment** | 2 | Low | `AccountScreen` medicine/activity list dividers span full card width instead of aligning cleanly with text start on inset icon rows. |
| **Total Measured Issues** | **33** | — | — |

---

## Detailed Findings by Screen & Component

### 1. Shared Theme & Typography (`ui/theme/Type.kt`)
- **TYPO-01 (High):** `bodyMedium` is `16.sp` (`lineHeight = 24.sp`), violating the rule that body text must be `18px/18sp` or larger with a `1.55–1.60` line-height.
- **TYPO-02 (High):** `labelSmall` is `13.sp` (`lineHeight = 18.sp`), violating the rule that captions and small labels must be `14px/14sp` or larger with a `1.40` line-height.
- **TYPO-03 (Medium):** `labelLarge` (`16.sp / 22.sp = 1.375`) and `labelMedium` (`14.sp / 20.sp = 1.428`) deviate slightly from the exact `1.40` caption/label line-height token.
- **TYPO-04 (Low):** Missing shared non-breaking unit formatter (`\u00A0`) in `Type.kt` to guarantee numbers and clinical units (`mg/dL`, `mmHg`, `mg`, `days`) never break across lines.

### 2. Shared Buttons, Chips & Inputs (`ui/components/Buttons.kt`, `CardsAndBadges.kt`, `Inputs.kt`)
- **BTN-01 (High):** `SehatPrimaryButton`, `SehatSecondaryButton`, `SehatGhostButton`, and `SehatDestructiveButton` all enforce `maxLines = 1, overflow = TextOverflow.Ellipsis`. At `320x568`, labels like `"Simulate accept (Demo)"`, `"Download all data (JSON)"`, and `"Share care card"` clip with `...` instead of wrapping evenly across two balanced lines.
- **BTN-02 (Medium):** `SehatSecondaryButton` uses `PaddingValues(horizontal = 18.dp, vertical = 12.dp)` instead of the required `horizontal = 20.dp` button padding token.
- **BTN-03 (Medium):** `SehatSecondaryButton`, `SehatGhostButton`, and `SehatDestructiveButton` place `Icon` and `Text` directly inside `ButtonScope` without an inner `Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center)`.
- **INP-01 (Medium):** `SehatTextField` (`Inputs.kt`) renders `errorText` / `helperText` only when non-null, shifting subsequent form fields down by ~22dp when inline validation errors appear.
- **CHIP-01 (Low):** `StatusChip` and `DemoPillBadge` (`CardsAndBadges.kt`) do not enforce a shared `defaultMinSize(minHeight = 32.dp)` or `softWrap = false` on short status labels.

### 3. App Shell & Navigation (`ui/components/AppShell.kt`)
- **NAV-01 (Low):** Bottom navigation pill and top header action badges need consistent `tabular()` numeral styling on queue/refill counts so digits do not jitter when counts change.

### 4. Dashboard Screen (`ui/screens/DashboardScreen.kt`)
- **DASH-01 (Medium):** Vital summary numbers (`148 mg/dL`, `128/82 mmHg`) should align number and unit on a shared baseline (`Modifier.alignByBaseline()`) and use non-breaking spaces (`148\u00A0mg/dL`, `128/82\u00A0mmHg`) so narrow `320dp` cards never orphan `"mg/dL"` or `"mmHg"` onto a new line.

### 5. Log Reading Screen (`ui/screens/LogReadingScreen.kt`)
- **LOG-01 (Medium):** Input mode selector (`Keypad`, `Photo scan`, `Voice`) at `320dp` uses `maxLines = 1` inside a 3-column `Row` with `8.dp` horizontal padding, crowding `"Photo scan"` at `320x568`.
- **LOG-02 (Low):** Large keypad display number and unit (`mg/dL` / `mmHg`) should align on `Alignment.Bottom` / `alignByBaseline()` with `tabular()` numerals.

### 6. Trends & Doctor Summary Screen (`ui/screens/TrendsScreen.kt`)
- **TREND-01 (Medium):** Filter dropdown button (`trends_filter_btn`) sets `softWrap = false, maxLines = 1` which can crowd next to the title on `320dp` widths.

### 7. Emergency Screen (`ui/screens/EmergencyScreen.kt`)
- **EMG-01 (High):** `"Call <Caregiver>"` (`emergency_call_family_btn`) and `"WhatsApp <Caregiver>"` buttons use `maxLines = 1, overflow = TextOverflow.Ellipsis`, which can truncate caregiver names on narrow screens (`320x568`). Medical and emergency actions must never truncate names or warnings.

### 8. Profile / Account Screen & Modals (`ui/screens/AccountScreen.kt`, `ui/screens/ProfileModals.kt`)
- **PROF-01 (High):** `ProfileStickyTabBar` places 5 tabs (`Overview`, `Health`, `Medicines`, `Activity`, `Privacy`) in a non-scrollable `Row` with `.weight(1f)` and `maxLines = 1`. At `320x568` and `360x800`, each tab gets `< 58dp` width, clipping `"Overview"` and `"Medicines"`.
- **PROF-02 (High):** In `ProfileHeaderHeroCard`, `"Edit profile"` and `"Share care card"` are side-by-side `.weight(1f)` buttons at `320x568`, causing `"Share care card"` to truncate. Stacking below `360.dp` eliminates truncation.
- **PROF-03 (Medium):** `change_avatar_photo_btn` has `.size(34.dp)` without `.minimumInteractiveComponentSize()`, failing the `48dp` minimum touch target rule.
- **PROF-04 (Low):** Multi-line helper/caption blocks in `ProfileModals.kt` (lines 979, 1133) and `AccountScreen.kt` (line 2025) use `TextAlign.Center` instead of left alignment (`TextAlign.Start`).
- **PROF-05 (Low):** Inset list dividers in `ProfileActivityTabContent` span full width instead of aligning with the text start (`start = 22.dp`) past the status dot indicator.

---

## Visual Evidence (Before Fixes)
- Saved before-state alignment diagram to `/docs/ui/screenshots/before-alignment-offenders.svg`.
