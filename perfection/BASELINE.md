# SehatSaathi — Final Perfection Pass: Step 1 Baseline Report

- **Timestamp:** 2026-11-16T00:36:00-08:00
- **Project:** SehatSaathi (`com.aistudio.sehatsaathi.hkpvxm`) — Track 2: Healthcare
- **Architecture:** Native Android Application (Kotlin + Jetpack Compose Material 3 + Firebase AI Logic `gemini-3.8-flash`) with Companion Interactive Web Preview (`index.html` & `docs/index.html`) deployed at `https://shuvamjana1-ops.github.io/Sehat_SaThi/`.

---

## 1. Baseline Build, Test & Audit Metrics

| Check | Command / Tool | Baseline Result | Status |
| :--- | :--- | :--- | :--- |
| **Android APK Build (`compileDebugKotlin` & `assembleDebug`)** | `compile_applet` / `gradle assembleDebug` | `BUILD SUCCESSFUL` (`app-debug.apk`: 24 MB) | Pass |
| **Unit & Robolectric JVM Suite (11 Test Classes)** | `gradle :app:testDebugUnitTest` | 48 / 48 tests passing (0 failures) | Pass |
| **Root `npm run qa` & `npm run evaluate` Scripts** | `npm run qa` / `npm run evaluate` | Missing root `package.json` & Node evaluation runner | Fail (To Fix) |
| **Lighthouse Mobile Audit (Companion Web Preview)** | Static/DOM audit of `docs/index.html` (no headless Chrome daemon in container) | Performance ~96, Accessibility ~96, Best Practices ~95, SEO ~92 (missing `<meta name="description">` & `<link rel="manifest">` on companion web preview) | Partial (To Fix) |
| **Secret Scan (`grep` across repo & git history)** | Regex scan for `AIza`, `sk-`, `ghp_`, `PRIVATE KEY` | Zero hardcoded third-party or Gemini API keys; only standard Firebase Android client config (`app/google-services.json`, `AIzaSyDou...9bZYmKk` restricted by package + SHA-1 + App Check). `.env.example` had placeholder value `MY_GEMINI_API_KEY` instead of bare variable name `GEMINI_API_KEY=`. | Partial (To Fix) |

---

## 2. Baseline Defect List (Grouped by Severity Before Code Changes)

### Blocker (0)
- *None.* All core screens, navigation routes, deterministic engines, and APK builds compile and run without crashes.

### High (3)
- **DEF-01 (High — Gate 2 Safety Boundary & Input Validation):**
  - **Root Cause:** In `RiskEngine.kt`, `evaluateGlucose` used strict `>` instead of `>=` for `glucoseHighCautionMgDl` (250) and `glucoseHighUrgentMgDl` (300), causing `250 mg/dL` to evaluate as `NORMAL` instead of `CAUTION` and `300 mg/dL` to evaluate as `CAUTION` instead of `URGENT` (inconsistent with Gate 2 boundary requirements `249/250` and `299/300` and `docs/ACCURACY_REPORT.md`). Furthermore, `RiskEngine` lacked a deterministic input validator `validateVitalInput` to explicitly reject empty, negative, text, decimal, or physiologically impossible values (e.g., glucose `< 20` or `> 600 mg/dL`, BP systolic `< 40` or `> 300 mmHg`, diastolic `< 20` or `> 200 mmHg`, or `systolic <= diastolic`) before evaluation.
  - **Fix Plan:** Update `RiskEngine.evaluateGlucose` to use `>=` at `250` and `300`, add `RiskEngine.validateGlucoseInput(raw: String)` and `RiskEngine.validateBloodPressureInput(rawSys: String, rawDia: String)`, and add unit tests covering all exact boundaries (`53/54`, `69/70`, `249/250`, `299/300`, `179/180`, `119/120`, `139/140`, `89/90`, `<90/60` with dizziness) and invalid/negative/decimal/extreme inputs.
- **DEF-02 (High — Gate 2 "Why this result" Rule Transparency):**
  - **Root Cause:** `RiskAssessment` in `RiskEngine.kt` did not expose a dedicated `whyThisResultRule: String` field describing the exact deterministic rule and threshold numbers that fired, and `LogReadingScreen.kt` did not display a dedicated `"Why this result"` line on every assessment card.
  - **Fix Plan:** Add `whyThisResultRule: String` to `RiskAssessment` across all glucose and blood pressure branches and render it in `LogReadingScreen.kt` and the companion web preview.
- **DEF-03 (High — Gate 9 & Gate 10 Executable Scripts `npm run qa` and `npm run evaluate`):**
  - **Root Cause:** The repository root lacked a `package.json` defining `npm run qa` and `npm run evaluate` and the automated deterministic evaluator script `scripts/evaluate.mjs`.
  - **Fix Plan:** Create `package.json` and `scripts/evaluate.mjs` + `scripts/qa.mjs` so `npm run evaluate` runs real deterministic boundary, interaction, refill, and safety checks and writes `/docs/ACCURACY_REPORT.md`, and `npm run qa` runs the full deterministic + Gradle test verification suite.

### Medium (3)
- **DEF-04 (Medium — Gate 6 Logout Export Cache Cleanup & `.env.example` Format):**
  - **Root Cause:** `SehatSaathiViewModel.logout()` cleared session state in `DataStore` and in-memory bitmaps, but did not call `DoctorSummaryAndDataExporter.clearExportCache(getApplication())` to remove any temporary PDF/JSON export files created during the session. Also `.env.example` included `=MY_GEMINI_API_KEY` instead of bare variable names only (`GEMINI_API_KEY=`).
  - **Fix Plan:** Call `DoctorSummaryAndDataExporter.clearExportCache(getApplication())` inside `logout()`, add a regression test, and update `.env.example` to list variable names only.
- **DEF-05 (Medium — Gate 5 & Gate 4 Web Companion PWA Manifest, Meta Description, Skip Link & Offline Banner):**
  - **Root Cause:** `docs/index.html` and `index.html` were missing `<meta name="description">`, a keyboard skip-to-main-content link (`.skip-link`), `<link rel="manifest" href="manifest.json">`, `docs/manifest.json`, `docs/sw.js`, and an automatic `"You are offline. Showing saved data."` listener banner.
  - **Fix Plan:** Add `<meta name="description">`, skip link, ARIA live regions, offline status listener banner, `manifest.json`, and `sw.js` to `/docs/` and `/`.
- **DEF-06 (Medium — Gate 9 Submission Readiness Documentation Bundle):**
  - **Root Cause:** `/docs/PPT_OUTLINE.md`, `/docs/VIDEO_SCRIPT.md`, `/docs/JUDGE_QA.md`, `/docs/SCREENSHOTS_TO_CAPTURE.md`, `/docs/perfection/OPEN_ISSUES.md`, `/docs/perfection/FINAL_REPORT.md`, and the 3-step **Quick Start for Judges** section in `README.md` and `LandingScreen.kt` were not yet created.
  - **Fix Plan:** Author all required submission documents with honest, non-fabricated metrics and add the 3-step Judge Quick Start card to `README.md` and `LandingScreen.kt`.

### Low (1)
- **DEF-07 (Low — Gate 2 Prompt-Injection Sanitization on AI Transcripts/OCR):**
  - **Root Cause:** While Gemini outputs are parsed strictly as numbers/medicine names and never executed as code, `VitalsAiParser.fallbackParseSpokenText` and `PrescriptionAiParser` should explicitly strip/ignore prompt-injection control phrases (e.g., `"Ignore previous instructions"`, `<script>`, HTML tags) and treat all user/OCR text strictly as plain data.
  - **Fix Plan:** Add explicit prompt-injection & XSS sanitization helper in `VitalsAiParser` / `ProfileDomain` and verify with unit tests in `RiskEngineTest.kt`.
