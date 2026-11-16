# SehatSaathi - Bug Tracking Log (`BUGS.md`)

This log tracks identified bugs, severities, reproduction steps, fixes, and regression test coverage.

## Open Bugs
- *None currently open.* All unit, Robolectric, and integration test suites pass successfully.

## Resolved / Fixed Bugs
| Bug ID | Severity | Description & Steps to Reproduce | Expected Behavior | Actual Behavior | Fix Applied | Regression Test |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **BUG-001** | Medium | Staggered grid item wrapping on compact viewports during initial dashboard layout | Proper vertical stack without horizontal overflow | Mild clipping on 320px screens | Adjusted modifier constraints to use flexible fillMaxWidth with 600.dp max width clamping | `PolishHelpersTest.testDashboardResponsiveness` |
| **BUG-002** | Low | Caregiver status chip contrast in dark mode | WCAG AA compliant contrast ratio (>4.5:1) | Slightly muted text color | Updated color tokens to use high-readability scrim tokens (`GlassTokens.highContrastScrim`) | `CaregiverDomainTest.testCaregiverStatusContrast` |
