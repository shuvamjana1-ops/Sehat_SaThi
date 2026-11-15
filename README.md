# SehatSaathi

**SehatSaathi** is a prescription reader and health safety companion built with Kotlin, Jetpack Compose (Material 3), and Firebase AI Logic (`gemini-3.8-flash`) for people managing long-term conditions (diabetes and hypertension) and their families. All copy is written in clear, high-contrast English tailored for older adults.

---

## Core Safety Architecture

1. **AI perceives & explains — deterministic code decides:**
   - Gemini (`gemini-3.8-flash` via Firebase AI Logic + App Check) only reads meter photos, prescription photos, and spoken vitals phrases into structured JSON, and produces plain-language summaries of already-computed results.
   - All risk levels, emergency triggers, duplicate/interaction checks, refill status, and alert cooldowns are computed by deterministic rules (`RiskEngine`, `InteractionChecker`, `RefillEngine`).
2. **Mandatory user verification:**
   - Every AI-extracted reading or medicine line enters a confirmation step (`Please verify` when confidence `< 0.7` or any field is missing) before saving.
3. **Unknowns are never treated as safe:**
   - Medicines, interactions, or stock counts not present in the bundled dataset display `"Not in our database. Please ask your pharmacist."`
4. **Non-overlapping medical disclaimer:**
   - A persistent footer stays anchored in the app shell:
     `SehatSaathi is a support tool, not a doctor. In an emergency call 112.`

---

## Implemented Features

- **Landing, Auth & Demo Mode:** One-tap Demo Mode seeded with 30 days of readings, medicines, alerts, and consented family contacts (`Sunita Devi`), plus local email/password sign-in validation.
- **Responsive App Shell & Accessibility:** Adaptive navigation across compact phones (floating pill bar with raised `Log reading` action), medium tablets (rail), and expanded screens (collapsible sidebar + `Ctrl/Cmd+K` command palette), 48dp+ touch targets, Manrope typography, and tabular numerals.
- **Log Reading, Risk Engine & Emergency Care:**
  - Photo scan (camera/gallery via Photo Picker + Gemini structured extraction), voice phrase parsing (on-device `SpeechRecognizer` + Gemini + regex offline fallback), and large keypad entry.
  - Deterministic blood sugar & BP evaluation, read-aloud (`Listen` via `TextToSpeech`), calm emergency screen with 112 dialer button, WhatsApp alert launch, and alert cooldowns.
- **Prescription Scanner & Drug Safety:**
  - 4-step workflow (`Capture` → `Review` → `Safety Checks` → `Schedule`), pre-upload blur/brightness check, bundled 58-drug dataset (`sample_drugs_dataset.json`), interaction & duplicate-ingredient detection, cheaper Jan Aushadhi generic options, and spoken daily schedule reminders (`Morning` / `Afternoon` / `Night`).
- **Refill Engine, Trends & Doctor Summary PDF:**
  - Deterministic stock & refill tracking (`OUT`, `DUE`, `SOON`, `OK`, `UNKNOWN`), restock/adjust/snooze actions, and pharmacy WhatsApp refill intent.
  - 7-day and 30-day glucose & blood pressure charts, adherence tracking, 1-page Doctor Summary PDF export (`PdfDocument` with PDF 1.4 fallback), and shareable intents.
- **Account, Privacy & Offline Queue:**
  - Patient/pharmacy profile, reminder times & quiet hours, clinician-reviewable thresholds, caregiver consent lifecycle, re-authentication-gated JSON data export, and `DELETE`-confirmed local wipe.
  - Live `ConnectivityManager` tracking, offline simulation toggle, automatic local queuing of meter/prescription/voice scans when offline, one-tap sync when back online, and direct family call fallback for urgent offline readings.

---

## Simulated (`Demo`) vs. Real Capabilities

| Capability | Status |
|---|---|
| Meter photo, Rx photo & plain-language AI | Real (`Firebase.ai` with `gemini-3.8-flash`) |
| Risk engine, interactions, refills & PDF/JSON export | Real deterministic Kotlin & Android APIs |
| Phone dialer (`112` / family), Photo Picker, `TextToSpeech`, `ConnectivityManager` | Real Android system integrations |
| Local sign-in, caregiver consent invites, WhatsApp send confirmation & 5-minute escalation timer (20s demo) | Clearly labeled **Demo** in the UI |

---

## Running Unit & Robolectric Tests

```bash
gradle :app:testDebugUnitTest
```

Includes JVM & Robolectric tests across `RiskEngineTest`, `InteractionCheckerTest`, `RefillEngineTest`, `OfflineQueueTest`, and `ExampleRobolectricTest`.

*Sample drug data in `app/src/main/assets/sample_drugs_dataset.json` is illustrative only and is not a substitute for clinical pharmacy references.*
