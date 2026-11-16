# SehatSaathi: 10-Slide Presentation Outline (Track 2: Healthcare)

Each slide is written in plain language so you can speak naturally to judges without sounding rehearsed.

---

## Slide 1: Title and What We Built
- **Title:** SehatSaathi: Daily support for blood sugar, blood pressure, and medicines
- **Subtitle:** Built for older adults in India and the family members who help care for them between clinic visits.
- **On screen:** Sunita Devi's daily summary card (`138 mg/dL` fasting blood sugar, `128/82 mmHg` blood pressure, `3 of 4` medicines taken) next to the **Rule verified** badge.

---

## Slide 2: What Goes Wrong Between 10-Minute Clinic Visits
- **Points to cover:**
  1. Many older adults with diabetes and high blood pressure take 4 to 8 tablets a day across changing brand names (`Amaryl`, `Glycomet`, `Telma`, `Ecosprin`).
  2. When a doctor switches brands on a handwritten prescription, patients sometimes take both the old and new brand without knowing both contain Glimepiride or Telmisartan.
  3. Dangerous readings, like a blood sugar of `54 mg/dL` at 2 AM or blood pressure of `180/120 mmHg`, get written in a paper notebook instead of triggering clear first-aid steps and a call to family.

---

## Slide 3: Why We Separate AI Perception from Safety Rules
- **Points to cover:**
  - Gemini is helpful at reading messy handwriting, reading a glucometer screen, and explaining medical terms in plain words.
  - Gemini should never decide whether a blood sugar reading is dangerous or whether two tablets conflict.
  - **Our rule:** AI reads and explains. Fixed Kotlin code and lookup tables make every safety decision.

---

## Slide 4: How the App Works in 4 Steps
- **Points to cover:**
  1. **Take a photo of your prescription, or type it in.** Anything with confidence below `0.70` is marked **Please verify**, unreadable fields stay blank, and nothing is saved until you confirm it.
  2. **Check for duplicate active ingredients and interactions.** Fixed rules check 100 common Indian medicines and 30 interaction pairs.
  3. **Log blood sugar and blood pressure.** Fixed thresholds check every number and show the exact rule that fired.
  4. **Track refills and alert family.** Taking a tablet lowers your stock count; skipping a dose never lowers stock and never tells you to double the next dose.

---

## Slide 5: The Deterministic Risk Engine
- **Points to cover:**
  - **Blood sugar rules:** Below `54 mg/dL` is an emergency. `54 to 69 mg/dL` is urgent low. `70 to 249 mg/dL` is normal. `250 to 299 mg/dL` is caution. `300 mg/dL` or higher is urgent high.
  - **Blood pressure rules:** Below `90/60 mmHg` with dizziness triggers an alert. `90 to 139` over `60 to 89 mmHg` is normal. `140/90 mmHg` or higher is caution. `180/120 mmHg` or higher is urgent or emergency.
  - Every result shows a **Why this result** box with the exact rule and ends with *"Ask your doctor or pharmacist."*
  - Empty, negative, decimal, text, and out-of-range inputs are rejected and never shown as safe.

---

## Slide 6: Indian Brand Names, Duplicate Salts, and Generic Prices
- **Points to cover:**
  - Our bundled sample dataset (`sample_drugs_dataset.json`, clearly labeled sample data) maps 100 Indian brand names (`Glycomet`, `Amaryl`, `Telma`, `Augmentin`, `Dolo 650`) to their active ingredients.
  - If two brands share the same active ingredient, the app warns you before you take both.
  - Generic Jan Aushadhi price ranges are shown only when both the active ingredient and the exact strength match (`Metformin 500 mg` never matches `Metformin 850 mg`).

---

## Slide 7: Designed for Older Hands and Eyes
- **Points to cover:**
  - **Manrope font with tabular numbers (`tnum`):** `18sp` minimum body text, `14sp` minimum labels, and `48dp to 56dp` buttons.
  - **Status never relies on color alone:** Every status badge includes a color, an icon, and a clear word (`Normal`, `Caution`, `Urgent`, `Emergency`).
  - **Simple Mode:** One tap at the top reduces the app to the three daily screens (**Today**, **Log**, **Medicines**) and lets the user listen to their schedule aloud.
  - **Solid red emergency screen:** Opaque `#991515` background with no floating widgets, showing **Call 112** and **Call Family** without scrolling.

---

## Slide 8: Family View, Privacy Controls, and Account Switching
- **Points to cover:**
  - **Family & Caregiver Hub:** Switch between **Patient View** and **Caregiver View** to review linked summaries and acknowledge open alerts.
  - **Consent controls:** Patients choose what each caregiver can see (vitals, medicines, or emergency alerts only) and can revoke access in one tap.
  - **Demo and personal accounts:** Judges can explore Sunita Devi's demo profile, log out to create a personal account, and switch back to the demo account anytime.

---

## Slide 9: Testing, Verification, and Honest Limits
- **Points to cover:**
  - **51 automated JUnit and Robolectric tests** pass across 3 consecutive runs.
  - **70 deterministic clinical checks** pass (`100%`) via `npm run evaluate` (`docs/ACCURACY_REPORT.md`).
  - **Works offline:** Risk classification, dose tracking, refill math, and spoken number parsing work with no internet connection.
  - **What it cannot do:** It is a support tool, not a doctor. The 100-medicine dataset is a sample, and family alerts open your phone's WhatsApp, SMS, or dialer app for a final tap.

---

## Slide 10: Live Demo Walkthrough
- **3-minute demo flow:**
  1. Tap **Try demo** and run the **Low sugar at 2 AM** scenario (`55 mg/dL` with shakiness) to show the solid red Emergency Screen and family alert.
  2. Open **Medicines**, scan the sample prescription, and show the duplicate-ingredient warning and exact-strength generic price comparison.
  3. Switch to **Caregiver View** and preview the 1-page **Doctor Visit Summary PDF**.
