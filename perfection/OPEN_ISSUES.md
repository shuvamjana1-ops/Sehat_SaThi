# SehatSaathi — Open Issues & Manual Hardware Verification Log

- **Project:** SehatSaathi (`com.aistudio.sehatsaathi.hkpvxm`, Track 2: Healthcare)
- **Audit Date:** 2026-11-16
- **Open Blocker Issues:** `0`
- **Open High Issues:** `0`
- **Open Medium Issues:** `0`
- **Open Low / Architectural Scope Notes:** `2`
- **Hardware / Third-Party App Items Requiring Real-Phone Verification:** `5` (documented in [`docs/qa/MANUAL_CHECKLIST.md`](../qa/MANUAL_CHECKLIST.md))

---

## 1.Resolved Baseline Defects (Fixed & Regression-Tested in This Pass)

| ID | Initial Severity | Gate | Summary | Resolution & Regression Evidence |
| :--- | :---: | :---: | :--- | :--- |
| **DEF-01** | **High** | Gate 2 | `RiskEngine.evaluateGlucose` used strict `>` at `250` and `300`, classifying `250 mg/dL` as `NORMAL` and `300 mg/dL` as `CAUTION` instead of `>= 250` (`CAUTION`) and `>= 300` (`URGENT`). | Fixed in `RiskEngine.kt` (`mgDl >= config.glucoseHighCautionMgDl` and `mgDl >= config.glucoseHighUrgentMgDl`). Verified in `RiskEngineTest.glucoseBoundaries_53_54_69_70_249_250_299_300` and `npm run evaluate`. |
| **DEF-02** | **High** | Gate 2 | Result card did not display the explicit `"Why this result"` rule string, out-of-range integers passed directly to `evaluateGlucose`/`evaluateBloodPressure` lacked a range guard, and rescue copy mentioned `15 g / 3 glucose tablets`. | Added `whyThisResultRule` to `RiskAssessment` + rendered `"Why this result"` box (`testTag = "why_this_result_rule"`) in `LogReadingScreen.kt`; added `validateGlucoseInput` / `validateBloodPressureInput` + range guards (`20..600 mg/dL`, `40..300 / 20..200 mmHg`, `sys > dia`) so invalid values are never marked safe; replaced specific gram/tablet counts with non-prescriptive guidance ending with *"Ask your doctor or pharmacist."* |
| **DEF-03** | **High** | Gate 9 / 10 | Repo root lacked `package.json` scripts (`npm run qa` and `npm run evaluate`). | Created `/package.json`, `/scripts/evaluate.mjs` (60/60 deterministic checks -> `docs/ACCURACY_REPORT.md`), and `/scripts/qa.mjs`. |
| **DEF-04** | **Medium** | Gate 6 | `logout()` cleared session state in `DataStore` but did not purge temporary PDF/JSON export files in `cacheDir/exports`, and `.env.example` included `# GEMINI_API_KEY=MY_GEMINI_API_KEY`. | Updated `resetDemoDataInternal()` in `SehatSaathiViewModel.kt` to call `DoctorSummaryAndDataExporter.clearExportCache(getApplication())` on every logout/reset, and updated `.env.example` to list `GEMINI_API_KEY=` (variable name only). |
| **DEF-05** | **Medium** | Gate 4 / 5 | Companion web pages (`docs/index.html` and `index.html`) lacked `<meta name="description">`, PWA `manifest.json` + service worker `sw.js`, keyboard skip link, and offline status banner. | Added `<meta name="description">`, `manifest.json`, `sw.js`, `.skip-link`, `#offlineBanner` (*"You are offline. Showing saved data."*), and the 3-step Quick Start for Judges to `docs/index.html` and `index.html`. |
| **DEF-06** | **Medium** | Gate 9 | Missing submission docs (`/README.md`, `/docs/PPT_OUTLINE.md`, `/docs/VIDEO_SCRIPT.md`, `/docs/JUDGE_QA.md`, `/docs/SCREENSHOTS_TO_CAPTURE.md`, `/docs/perfection/FINAL_REPORT.md`, `/docs/perfection/OPEN_ISSUES.md`). | Created all required Gate 9 and Perfection Pass documents and visual evidence SVGs in `docs/perfection/screenshots/`. |
| **DEF-07** | **Low** | Gate 2 / 6 | `VitalsAiParser.parseVoiceTranscript` truncated input but did not strip HTML/script tags or prompt-injection phrases before regex fallback. | Added `VitalsAiParser.sanitizeUntrustedTranscript()` and regression test `offlineVoiceFallback_parsesGlucoseAndBp_andSanitizesPromptInjection` in `RiskEngineTest.kt`. |

---

## 2. Open Architectural Scope Notes (Non-Blocking, By Design)

### OPEN-01 — Bundled 100-Medicine Dataset Is a Sample Formulary (`verified: false`)
- **Severity:** Low (By Design / Documented Safety Constraint)
- **Impact:** Medicines outside the 100-entry `sample_drugs_dataset.json` are flagged as **`Unknown medicine — verify with pharmacist`** rather than matched to a generic or interaction rule.
- **Steps to Reproduce:**
  1. Open **Medicines** tab.
  2. Type an uncommon specialty brand not in the 100-item list (e.g., `"Tacrolimus 1mg"`).
  3. Observe the caution badge: *"Unknown — not in sample database. Ask your doctor or pharmacist."*
- **Proposed Fix (Post-Hackathon Production):** Connect `DrugDatasetRepository` to a licensed, pharmacist-verified CDSCO / Jan Aushadhi national formulary database while keeping the `"Unknown is never marked safe"` fallback invariant.

### OPEN-02 — Caregiver Alerts Use Client-Side Pre-Filled Intents Rather Than a Paid Server SMS Gateway
- **Severity:** Low (By Design / Zero-Backend Constraint)
- **Impact:** Family alerts open WhatsApp, SMS, or the Android Phone Dialer with a pre-filled clinical summary (`launchWhatsAppAlert` / `launchSafeDialer`), which requires 1 final tap from the user inside WhatsApp/Dialer (simulated escalation countdowns are labeled `Demo`).
- **Steps to Reproduce:**
  1. Log a critical reading (`53 mg/dL`) or run the **"Low sugar at 2 AM"** demo scenario.
  2. Tap **"WhatsApp Rajesh (Demo)"** on the result card.
  3. Observe that Android launches WhatsApp/Share sheet with the pre-filled message ready to send.
- **Proposed Fix (Post-Hackathon Production):** Add an opt-in backend webhook (Twilio / WhatsApp Business Cloud API) for automated background caregiver dispatch when an emergency reading is confirmed.

---

## 3. Manual Test Needed on Physical Android Phone (Cannot Be Verified in Container)

| ID | Category | Why Manual Verification Is Needed | Checklist Reference |
| :--- | :--- | :--- | :--- |
| **MAN-01** | **Real Camera Optics** | Glare on glossy tablet strips, crumpled paper prescriptions, and angled 7-segment glucometer LCDs require physical camera testing with live Gemini vision. | `MANUAL_CHECKLIST.md` Items #3, #4 |
| **MAN-02** | **Real Room Microphone** | Android `SpeechRecognizer` with ambient room noise and regional Indian accents (`"Aaj sugar 240 hai"`) requires a physical microphone. | `MANUAL_CHECKLIST.md` Item #5 |
| **MAN-03** | **System Speaker TTS** | `TextToSpeech` audio clarity and volume Ducking on physical phone speakers require ear verification. | `MANUAL_CHECKLIST.md` Item #6 |
| **MAN-04** | **Native App Handoffs** | End-to-end delivery into installed WhatsApp (`https://wa.me/...`), carrier SMS app, and Android Phone Dialer (`tel:112`) requires a handset with those apps installed. | `MANUAL_CHECKLIST.md` Items #7, #8 |
| **MAN-05** | **Android Print Spooler** | Printing the 1-page Doctor Summary PDF to a Wi-Fi printer or system Save-as-PDF spooler requires the Android system print UI. | `MANUAL_CHECKLIST.md` Item #9 |
