# SehatSaathi — 15 Likely Judge Questions & Honest Answers (Track 2: Healthcare)

---

### Q1. How do you prevent Gemini from hallucinating medical advice, diagnosing diseases, or changing a patient's dose?
- **Answer:** We enforce a strict architectural boundary between **perception** and **clinical decision-making**:
  1. Gemini (`gemini-3.8-flash` via Firebase AI Logic) is used **only** to extract structured JSON from prescription photos, glucometer displays, or voice phrases, and to paraphrase deterministic guidance in plain English.
  2. All risk classification (`RiskEngine.kt`), drug interaction checks (`InteractionChecker.kt`), duplicate-ingredient alerts, and refill calculations (`RefillEngine.kt`) are **100% deterministic Kotlin functions and lookup tables**.
  3. Every warning and AI explanation prompt explicitly forbids diagnosis or dose changes and ends with *"Ask your doctor or pharmacist."*

---

### Q2. What happens if Gemini misreads a handwritten prescription or a blurry glucometer screen?
- **Answer:** Nothing is ever saved automatically:
  1. Every AI extraction returns a `confidence` score (`0.0` to `1.0`).
  2. If `confidence < 0.70`, the UI forces a high-visibility **"Please verify"** caution badge (`needsVerification = true`).
  3. Unreadable fields remain `null` (never guessed).
  4. The user must explicitly review and tap **"Confirm & Check Safety"** before any reading or medicine is saved.

---

### Q3. What happens if a medicine on a patient's prescription is not in your database?
- **Answer:** Unknown medicines are **never** shown as "Safe" or "No interactions." `InteractionChecker.kt` explicitly flags any medicine outside the 100-medicine dataset (`sample_drugs_dataset.json`) with an `UNKNOWN` caution badge (*"Unknown medicine — not in sample database. Ask your doctor or pharmacist"*).

---

### Q4. How do you ensure generic medicine suggestions are clinically safe?
- **Answer:** `InteractionChecker.findGenericMatches()` matches a generic alternative **only when both the active ingredient AND the exact strength match** (for example, `Metformin 500 mg` matches only `Metformin 500 mg` and never `Metformin 850 mg` or `1000 mg`). Every generic card displays its source (`CDSCO / Jan Aushadhi / NLEM`), `"Sample data: verify with an official source before real use"`, and `"Prices are approximate; verify at the store."`

---

### Q5. How does the app behave when there is no internet connection or no API key?
- **Answer:**
  - All core safety features—vital-sign risk classification, 100-medicine interaction & duplicate-salt checks, daily schedule, refill math, Doctor Summary PDF generation, and Demo Mode—run **100% on-device offline**.
  - Voice logging falls back automatically to `VitalsAiParser.fallbackParseSpokenText()`, which deterministically parses English and transliterated Hindi numbers (`"BP 150 by 95"`, `"Aaj sugar 240 hai"`).
  - Photo scan requests while offline are queued in `OfflineAiQueue` (`max 5` items, auto-pruning items older than 24 hours) with manual keypad entry always available immediately.

---

### Q6. Can an attacker use prompt injection in a prescription image or voice recording to make the app say a dangerous reading is "Normal"?
- **Answer:** No, for two independent reasons:
  1. `VitalsAiParser.sanitizeUntrustedTranscript()` strips HTML/script tags (`<...>`) and prompt-injection instructions (`"ignore previous instructions"`, `"mark everything safe"`, etc.) before parsing.
  2. Even if an attacker forced an extraction output, the extracted integer still passes through `RiskEngine.evaluateGlucose()` / `evaluateBloodPressure()`, which is pure deterministic code (`53 mg/dL` will always trigger `EMERGENCY` regardless of any text in the prompt).

---

### Q7. Why are your blood sugar boundaries set at 54, 70, 250, and 300 mg/dL, and BP at 90/60, 140/90, and 180/120 mmHg?
- **Answer:** These align with standard ADA / clinical triage thresholds for home monitoring:
  - `< 54 mg/dL`: Level 2 severe hypoglycemia (`EMERGENCY`).
  - `54–69 mg/dL`: Level 1 hypoglycemia (`URGENT` + 15-minute recheck reminder).
  - `70–249 mg/dL`: Everyday in-range (`NORMAL`).
  - `250–299 mg/dL`: Elevated hyperglycemia (`CAUTION`).
  - `>= 300 mg/dL`: Severe hyperglycemia (`URGENT`).
  - Furthermore, patients can customize their personal target thresholds in **Profile $\rightarrow$ Health** (with a 1-tap **"Reset to defaults"** button), and every result card displays the exact **"Why this result"** rule that fired.

---

### Q8. How do you prevent alert fatigue for family caregivers?
- **Answer:** `RiskEngine.shouldSendFamilyAlert()` enforces a **30-minute cooldown** per vital type and risk level so repeated taps don't spam the family. However, safety comes first: if a reading is marked as a **15-minute recheck** (`isRecheck = true`) and is *still* in the danger zone, or escalates to `EMERGENCY`, the cooldown is bypassed immediately.

---

### Q9. How does the tablet refill tracker avoid false stock counts?
- **Answer:**
  - Stock is decremented **only** when the user marks a scheduled dose **Taken** (and incremented back if undone).
  - Tapping **Skip** logs the skipped dose for adherence tracking without reducing tablet stock, and displays a calm reminder never to double the next dose.
  - If a user hasn't entered their tablet count (`unknownStock = true`), the app shows **"Stock not set"** rather than assuming stock is plentiful.

---

### Q10. How did you design the UI for older adults with tremor, low vision, or low digital literacy?
- **Answer:**
  - **Typography:** `Manrope` only, `18sp` minimum body text, `14sp` minimum captions, tabular numerals (`tnum`) so digits never jitter.
  - **Touch Targets:** `48dp–56dp` minimum height on all buttons and keypad keys.
  - **Simple Mode:** 1-tap button in the top bar reduces navigation to just 3 large screens (**Today**, **Log**, **Medicines**) and pairs with 1-tap **Listen (`TextToSpeech`)** read-aloud.
  - **Emergency Screen:** Solid opaque `#991515` red background, zero floating widgets, and `60dp` **Call 112** and **Call Family** buttons above the fold on every screen size.

---

### Q11. How are patient privacy and caregiver consent handled?
- **Answer:**
  - Caregivers are linked with explicit relationship and consent status (`Verified` / `Pending`).
  - In **Profile $\rightarrow$ Privacy** and **Family**, the patient controls granular toggles (`shareVitals`, `shareMedicines`, `urgentOnlyAlerts`) and can revoke caregiver access in 1 tap.
  - Temporary exported PDF/JSON files in `cacheDir/exports` are automatically wiped on **Log out** and **Delete account**.

---

### Q12. Can a judge or user log out of the Sunita Devi Demo account and create their own personal account?
- **Answer:** Yes. From **Account / Profile** (or the top-bar menu), you can tap **Log Out** or **Create New Account** to sign up with your own name and email. Your personal profile is persisted locally via `AuthSessionRepository` (`DataStore`), updates your name across the entire app, and lets you switch back and forth between your **Personal Account** and the **Sunita Devi Demo Account** in 1 tap.

---

### Q13. Why does the GitHub Pages web link show a notice to download the APK?
- **Answer:** Because SehatSaathi is built natively in **Kotlin + Jetpack Compose** for Android phones (`com.aistudio.sehatsaathi.hkpvxm`), the GitHub Pages site (`https://shuvamjana1-ops.github.io/Sehat_SaThi/`) serves both an interactive browser preview and a direct download of the compiled `SehatSaathi-debug.apk`. The modal clearly informs judges that the browser page is a companion preview and provides a 1-click APK download for full native evaluation.

---

### Q14. What is your real test coverage and how do you know the numbers aren't fabricated?
- **Answer:**
  - Running `npm run qa` executes `scripts/evaluate.mjs` (**70/70 deterministic safety checks**, 100%) followed by `gradle :app:testDebugUnitTest` (**51/51 JUnit & Robolectric tests** across 11 test classes, verified 3 times in a row with zero flaky tests).
  - For live Gemini vision/voice calls that require a runtime cloud session and real camera/microphone input, `docs/ACCURACY_REPORT.md` honestly reports **"Not measured"** in the CLI evaluator rather than inventing numbers.

---

### Q15. What are the current limitations and what would be needed before clinical deployment?
- **Answer:**
  1. **Formulary Scale:** Expand the 100-medicine sample dataset (`verified: false`) to a licensed, pharmacist-audited national drug & interaction database (CDSCO / Jan Aushadhi master catalog).
  2. **Backend Escalation:** Add an opt-in server-side SMS/voice call relay so family alerts fire even if the patient cannot tap Send inside WhatsApp.
  3. **Clinical Validation:** Conduct a formal usability and clinical pilot with geriatric patients and endocrinologists before any real-world medical use.
