# PRD: SehatSaathi — Prescription Reader + Health Safety Loop for Chronic Patients

**Hackathon:** VibeX Codes | **Track:** 2 (Easy, Safe & Smart Healthcare for Everyone)
**Version:** 1.0 | **Document owner:** Product/Engineering Lead

---

## 1. Executive Summary
SehatSaathi ("health companion") is a mobile-first, voice-and-camera healthcare app for patients with long-term conditions (diabetes, hypertension) and their families. It combines the two loops described in the Track 2 brief into one connected product:

1. **Prescription Helper:** photo of a handwritten prescription → structured medicines → interaction check → cheaper generic options → a simple schedule spoken aloud in the patient's language.
2. **Health Safety Loop:** daily sugar/BP readings by photo, voice or tap → trend and risk detection → safe next-step guidance → instant alert to family or doctor.

**Why these are one product:** the prescription tells the system *what the patient takes*, and the readings tell it *how the patient is doing*. Together they can say things no single-feature app can, for example: "Your sugar has dropped twice this week after the evening dose. Show this to your doctor."

**Positioning:** a *safety and understanding assistant*, not a diagnostic tool. It never changes doses or prescribes. This is both a safety principle and a scoring advantage (see section 12).

---

## 2. Problem Statement
- **No help between visits:** patients see a doctor about once every three months. At 2 AM, when sugar crashes, nothing is watching their readings and families don't know what to do.
- **2-minute consultations and unreadable prescriptions:** handwritten prescriptions and lab reports are not understood. Patients take the wrong medicine at the wrong time, combine medicines that interact, or pay for expensive brands when low-cost generics exist.
- **Language barrier:** most health tools assume English literacy and comfort with typing.

---

## 3. Target Users & Personas

| Persona | Description | Key need |
|---|---|---|
| **Sunita Devi, 67 (Primary)** | Type-2 diabetic and hypertensive, Hindi-speaking, basic smartphone, limited reading ability | Understand tablets, take them on time, know what to do when readings are bad |
| **Rohit, 34 (Caregiver)** | Her son, works in another city | Know immediately if mother's reading is dangerous or doses are missed |
| **ASHA / rural health worker** | Visits 30+ households | Quickly log and review readings for many patients |
| **Dr. Mehta (Secondary)** | General physician with 2 minutes per patient | A one-page trend summary before the visit |

---

## 4. Goals & Success Metrics

**Product goals**
- A patient gets a spoken, understandable medicine schedule from a prescription photo in **under 30 seconds**.
- A dangerous reading triggers a family alert in **under 10 seconds**.
- A first-time elderly user completes "log my sugar" in **under 10 seconds** (judging criterion: simplicity).

**Measurable targets for the demo / testing**
| Metric | Target |
|---|---|
| Prescription medicine-name extraction (on 15 test prescriptions) | 85%+ correct, remainder flagged "please verify" |
| Interaction checker on known-pair test set (30 pairs) | 100% of seeded dangerous pairs flagged |
| Threshold alert correctness (50 synthetic readings) | 100% (rule-based, deterministic) |
| Voice reading capture in Hindi (20 utterances) | 90%+ |
| Time to log a reading | under 10 seconds |

---

## 5. Scope

### In scope (MVP)
- Prescription photo reading and structuring
- Interaction check and generic suggestions (curated dataset)
- Medicine schedule with voice playback and reminders
- Glucose and BP logging (photo OCR, voice, manual)
- Rule-based risk detection with safe-step guidance
- Family/doctor alerts
- Trend dashboard and doctor-visit summary export
- Hindi + English (Bengali as stretch)

### Out of scope (v1)
Diagnosis, dose recommendations, e-prescribing, payments or ordering medicines, integration with hospital EMR, real-time CGM hardware integration, any claim of regulatory approval.

---

## 6. Features & Requirements

### F1. Prescription Scanner (P0)
**Description:** User photographs a prescription (handwritten or printed). A multimodal model (Gemini Vision) extracts structured data.

**Requirements**
- Capture via camera or gallery; on-device image quality check (blur, dark, cropped) with a Hindi prompt to retake.
- Extract per medicine: name, strength, form (tablet/syrup/insulin), frequency, timing (before/after food), duration, special instructions.
- Each field has a **confidence level**. Low-confidence items are highlighted in amber with "Please verify with your doctor or pharmacist."
- User can correct any field by voice or tap before saving.
- The original image is stored next to the structured result for traceability.

**Acceptance criteria**
- Given a clear prescription with 4 medicines, all 4 appear in a structured list with timing.
- Given a blurry image, the app asks for a retake instead of guessing.

### F2. Interaction & Safety Checker (P0)
**Description:** Check all active medicines (including newly scanned ones) for known interactions and duplicates.

**Requirements**
- Normalize drug names to generic/active ingredients (brand → generic mapping).
- Check pairs against a curated interaction table (severity: Avoid / Caution / Monitor) with a plain-language reason.
- Detect **duplicate ingredients** (e.g., two brands containing paracetamol).
- Surface condition-aware cautions (e.g., NSAIDs with hypertension or kidney concerns) as "ask your doctor."
- **The interaction logic is deterministic (lookup table), not free-form LLM output.** The LLM may only *explain* a flagged result in simple language.
- Every warning ends with: "Do not stop any medicine on your own. Ask your doctor or pharmacist."

**Acceptance criteria**
- Seeded dangerous pair triggers a red alert with an explanation in Hindi and English.
- Unknown medicine → "Not in our database. Please ask your pharmacist." (never a silent pass).

### F3. Generic Alternative Finder (P0)
- Map brand medicines to the same-ingredient, same-strength generic (curated dataset based on publicly available Jan Aushadhi / government generic lists).
- Show approximate saving (marked "approximate; verify price at the store").
- Only suggest an equivalent when ingredient AND strength match; otherwise show nothing.
- Include a "Show this to your pharmacist" card (one screen listing the generic names).

### F4. Voice Medicine Schedule (P0)
- Generate a daily timetable: Morning / Afternoon / Night, before/after food, with large pill icons (colour/shape descriptors if available from the user's input).
- Speak in simple language: "सुबह नाश्ते के बाद नीली गोली लीजिए। रात को सोने से पहले सफ़ेद गोली।"
- Language selector (Hindi, English, Bengali stretch).
- Reminder notifications (PWA push / alarm) with **"Taken ✅ / Skip"** big buttons; missed doses reported to the caregiver (optional).

### F5. Vitals Logger — Photo, Voice, Manual (P0)
- **Photo:** picture of a glucometer or BP monitor; the model reads the numbers, and the user confirms ("Is it 182?").
- **Voice:** "Aaj sugar 240 hai" / "BP 150 by 95" → parsed into values with a confirmation card.
- **Manual:** big number pad fallback.
- Tag the reading: fasting / after meal / bedtime / symptoms (icons).
- Every OCR/voice value must be **confirmed by the user** before saving (safety).

### F6. Risk Engine & Safe-Step Guidance (P0, core differentiator)
**Principle:** alerts come from a **transparent, rule-based engine** with configurable thresholds, reviewed against standard clinical guidance. The LLM never decides whether a reading is dangerous.

| Condition | Default rule (configurable, clinician-reviewable) | App response |
|---|---|---|
| Low sugar | below 70 mg/dL | Red alert: "Sugar is low. If you are awake and can swallow, take 15 g fast sugar (e.g., 3 glucose tablets or 3 teaspoons sugar in water). Recheck in 15 minutes." Alert family. |
| Severe low | below 54 mg/dL or symptoms such as confusion/fainting | Emergency screen: call family and emergency number; do not give food if the person is unconscious. |
| High sugar | above 250 mg/dL (caution), above 300 mg/dL (urgent) | Advise hydration, check per doctor's plan, contact doctor; alert family at urgent level. |
| High BP | 140/90 and above: elevated; 180/120 and above: crisis | At crisis level: rest, recheck in 5 min; if symptoms such as chest pain, breathlessness, severe headache or weakness, call emergency. Alert family. |
| Low BP | below 90/60 with dizziness | Sit or lie down, hydrate, contact doctor. |

**Trend prediction (P1, rule-based statistics, honest naming):**
- Detect patterns: 3 consecutive rising readings, repeated lows at the same time of day, large day-to-day variability, missed-dose correlation.
- Output: "Your sugar after dinner has been above 250 on 4 of the last 5 days" and a "Likely to go out of range" early-warning flag, labelled as a **trend warning, not a diagnosis**.
- Implementation: moving averages / slope over the last N readings plus simple rules. (Optional stretch: a lightweight regression model.) Do not over-claim "AI prediction."

**Acceptance criteria**
- Reading of 62 mg/dL → red alert, safe-step voice message, family notified.
- Same reading on a different date does not duplicate the alert within a cooldown window (e.g., 30 min), but re-alerts if still low after recheck.

### F7. Family & Doctor Alerts (P0)
- Add caregiver contacts with consent (an invite link they accept).
- Alert channel for MVP: WhatsApp `wa.me` prefilled message (zero cost) plus in-app notification; optional SMS/FCM (stretch).
- Message example: "⚠️ Sunita Devi's sugar is 62 mg/dL (low) at 2:04 AM. Please call her now."
- Escalation: if the caregiver does not acknowledge within 5 minutes, alert the secondary contact (simulated timer for the demo).

### F8. Trends Dashboard & Doctor Summary (P1)
- Charts: sugar and BP over 7/30 days with target bands.
- One-page **Doctor Summary PDF**: current medicines, readings summary, alerts, adherence %, flagged patterns, suggested questions to ask the doctor.
- Shareable via WhatsApp.

### F9. Lab Report Explainer (P2 / stretch)
- Photo of a lab report → values extracted → each shown against reference range with a colour (normal/high/low) and a plain-language explanation. No diagnosis.

### F10. Health Worker Mode (P2 / stretch)
- ASHA-style multi-patient list sorted by risk level; quick-log flow.

### F11. Accessibility & Language (P0)
- Hindi-first UI, big fonts (18px+), high contrast, icon-led navigation, tap targets 56px+, read-aloud on every screen, voice input everywhere a number is needed.

### F12. Offline & Low-Bandwidth Behaviour (P1)
- Core logging, threshold alerts and medicine schedule work **offline** (rules run on-device).
- OCR/voice parsing queued until online; manual entry always available.
- Alerts needing the network show "Net nahi hai: call family directly" with a one-tap `tel:` button.

---

## 7. Key User Flows

**Flow 1: Onboarding (under 1 minute)**
Language → name/age → conditions (diabetes/BP icons) → add one family contact → done.

**Flow 2: Scan prescription**
Tap camera → capture → "Reading…" → list of medicines with confidence flags → user confirms/edits → interaction + generic results → "Listen" button plays the schedule → reminders created.

**Flow 3: Daily reading**
Home → tap "Sugar" → photo/voice/number → confirm value → instant result card (green/amber/red) → guidance spoken → if red, family is alerted.

**Flow 4: 2 AM emergency (demo hero scenario)**
Patient logs 55 mg/dL → full-screen red emergency → voice: "Sugar bahut kam hai. Turant 3 glucose goli ya cheeni paani mein lijiye." → WhatsApp alert to son with a call button → 15-minute recheck reminder.

**Flow 5: Doctor visit**
Open "Doctor Summary" → generate → share.

---

## 8. System Architecture

```
[React PWA (mobile-first)]
   |- Camera / Mic capture
   |- On-device Rules Engine (thresholds, schedule, cooldowns)  <- works offline
   |- IndexedDB (offline cache)  <-> sync
   v
[Backend: Node/Express or serverless functions]
   |- AI Gateway (keeps API keys server-side)
   |     |- Gemini Vision: prescription/vitals OCR -> JSON (schema-constrained)
   |     |- Gemini Audio/Text: voice reading parsing, plain-language explanations
   |- Drug Knowledge Service (curated JSON/SQL): brand->generic, interactions, price bands
   |- Alert Service (WhatsApp links, FCM push stretch, escalation timer)
   |- Report Service (PDF generation)
   v
[Database: Firebase Firestore or Supabase Postgres + Storage]
   Users, Patients, Medicines, Readings, Alerts, Contacts, Consents
```

**Design principles**
- **LLM for perception and explanation; deterministic code for safety decisions.**
- Structured outputs with JSON schema validation. Invalid output → retry once → fall back to manual entry.
- Keys never in client code; request rate limits; input size limits.
- Everything auditable: store raw image, extracted JSON, user corrections.

**Suggested stack:** React + TypeScript + Tailwind, Vite PWA, Dexie (IndexedDB), Node/Express or Firebase Functions, Firestore/Supabase, Gemini API, Recharts, jsPDF/pdf-lib, Web Speech API (`hi-IN`) for TTS.

---

## 9. Data Model (simplified)

- **User**: id, name, age, language, role (patient|caregiver|doctor|chw), createdAt
- **Patient**: id, userId, conditions[], thresholds{...}, emergencyNumber
- **CareLink**: patientId, contactId, channel, consentStatus, priority
- **Prescription**: id, patientId, imageUrl, rawExtraction(JSON), verifiedByUser(bool), date
- **Medicine**: id, prescriptionId, brandName, genericName, strength, form, frequency, timing, durationDays, confidence, active
- **DoseEvent**: id, medicineId, scheduledAt, status (taken|skipped|missed)
- **Reading**: id, patientId, type (glucose|bp), values{}, context (fasting|post-meal|bedtime), source (photo|voice|manual), confirmedByUser, timestamp
- **Alert**: id, patientId, level (info|caution|urgent|emergency), reason, sentTo[], acknowledgedAt
- **DrugRef** (static): ingredient, brands[], genericAvailable, price band, notes
- **InteractionRef** (static): ingredientA, ingredientB, severity, plainLanguageReason, source

---

## 10. AI Contracts (example schemas)

**Prescription extraction**
```json
{
  "image_quality": "good|poor",
  "doctor_name": "string|null",
  "date": "string|null",
  "medicines": [
    {
      "name_as_written": "string",
      "normalized_name": "string|null",
      "strength": "string|null",
      "form": "tablet|capsule|syrup|injection|insulin|other",
      "frequency": "OD|BD|TDS|QID|SOS|other",
      "timing": "before_food|after_food|bedtime|morning|other|null",
      "duration_days": "number|null",
      "confidence": 0.0,
      "needs_verification": true
    }
  ],
  "warnings": ["string"]
}
```

**Vitals extraction**: `{ "type": "glucose|bp", "values": {"mg_dl": 182} or {"sys":150,"dia":95}, "confidence": 0.0, "unit": "string" }`

**Rule:** model must output `null` for unreadable fields. Hallucinating a value is a bug. Confidence under 0.7 forces user verification.

---

## 11. Safety, Ethics, Privacy & Compliance

| Risk | Mitigation |
|---|---|
| OCR misreads a medicine/dose | Confidence flags, mandatory user confirmation, "verify with pharmacist" banner, original image kept side by side |
| Wrong medical advice | No dosing or diagnosis; deterministic rules; only first-aid style safe steps from standard guidance; clinician-reviewable thresholds file |
| Missed unknown interaction | "Unknown" state never shown as "safe"; disclaimer on every result |
| LLM hallucination | LLM does not decide risk; schema validation; null-on-uncertainty |
| Privacy of health data | Encryption in transit/at rest, minimal data collection, explicit caregiver consent, delete-my-data button, no data sold or used for ads |
| Alert fatigue | Cooldowns, severity levels, only urgent/emergency go to family by default |
| Over-trust | Persistent footer: "SehatSaathi is a support tool, not a doctor. In an emergency call 112 / your doctor." |
| Regulatory | Positioned as an informational/wellness assistant; do not claim to be a medical device |

*Note for the team:* thresholds and first-aid steps above reflect commonly published guidance (e.g., the "15-15 rule" for hypoglycemia, BP categories). Cite your sources on a slide and, if possible, have a doctor or pharmacist friend review them. This earns trust with judges.

---

## 12. Non-Functional Requirements
- **Performance:** prescription result under 15 s on 4G; reading alert under 3 s (on-device rule evaluation is instant).
- **Reliability:** rules engine works with zero connectivity.
- **Usability:** WCAG AA contrast, 18px+ text, 56px tap targets, voice alternative for every input.
- **Cost:** designed to run within free tiers (Gemini free quota, Firebase Spark/Supabase free); show a cost-per-user estimate in the PPT.
- **Security:** server-side API keys, input validation, rate limiting, HTTPS only.
- **Compatibility:** Chrome on low-end Android; installable PWA.

---

## 13. Mapping to the Judging Scorecard (115 points)

| Criterion | Pts | How SehatSaathi earns it |
|---|---|---|
| Real-world problem & usefulness | 25 | Directly implements both examples from the brief; persona-driven; clear pain (2 AM crash, unreadable prescriptions, costly brands) |
| How well tech works | 25 | Real DB, multimodal OCR with schema validation, deterministic rules engine, interaction dataset, trend logic, alerts, PDF export, tested with metrics |
| Super easy to use | 20 | Voice + photo + big buttons, Hindi-first, read-aloud, 10-second logging |
| Speed, safety, readiness | 15 | Offline rules engine, confirm-before-save, no silent "safe", privacy controls, cooldowns, cost-aware |
| Clear & honest PPT | 15 | Show metrics, limitations, data sources; honest "support tool, not a doctor" |
| Bonus video | +15 | Scripted 2–3 min live demo (see below) |

---

## 14. Differentiators (what makes it win)
1. **One connected loop:** prescription → schedule → readings → alerts → doctor summary.
2. **Safety by design:** LLM perceives and explains; deterministic code decides. State this explicitly in the PPT.
3. **Honest uncertainty:** low-confidence flags, "unknown ≠ safe."
4. **Truly offline-capable alerts** and a manual fallback for every AI step.
5. **Measured results:** show your accuracy table from the test sets.
6. **Real family loop:** the 2 AM alert is a memorable live demo.

---

## 15. Demo Script (2–3 minutes, for the bonus video)
1. **(0:00–0:20)** One-sentence problem + persona Sunita.
2. **(0:20–1:00)** Photograph a handwritten prescription → medicines extracted → one amber "verify" flag → interaction warning (seeded pair) → cheaper generic card → tap "Listen" and play the Hindi schedule.
3. **(1:00–1:40)** Say a sugar reading by voice ("Aaj sugar 240 hai") → confirm → amber result. Then photograph a glucometer showing 58 → red emergency screen → Hindi safe-steps voice.
4. **(1:40–2:10)** Show the caregiver phone receiving the WhatsApp alert with a call button.
5. **(2:10–2:40)** Open the 7-day trend chart with a flagged pattern + generate the Doctor Summary PDF.
6. **(2:40–3:00)** Turn on airplane mode: log a reading, alert logic still works. Close with the limits statement and repo link.

---

## 16. Milestones (24–36 hour plan)

| Phase | Deliverable |
|---|---|
| Hours 0–3 | Repo, PWA shell, auth-less onboarding, design system (Hindi-first big UI) |
| Hours 3–9 | Prescription scanner (Gemini Vision → JSON → editable list) |
| Hours 9–13 | Drug dataset (about 60–100 common Indian medicines for diabetes/BP/pain/antibiotics), interaction + generic modules |
| Hours 13–18 | Vitals logger (photo/voice/manual) + rules engine + safe-step screens |
| Hours 18–22 | Alerts (WhatsApp links, escalation timer), medicine schedule + TTS + reminders |
| Hours 22–26 | Dashboard, trend flags, Doctor Summary PDF |
| Hours 26–30 | Offline mode, testing (accuracy tables), bug fixing, seed demo data |
| Hours 30–36 | PPT, README, 2–3 min video, link checks |

**Scope control:** if time runs short, cut F9 and F10 first, then trend prediction. Never cut the confirm-before-save step or the rules engine.

---

## 17. Test Plan
- **Prescription set:** 15 real/mock prescriptions (varied handwriting). Record extraction accuracy per field.
- **Interaction set:** 30 pairs (including 10 seeded dangerous, 10 safe, 10 unknown). Check expected severity.
- **Rules engine unit tests:** boundary values (69/70, 53/54, 249/250, 299/300, 179/180 etc.).
- **Voice set:** 20 Hindi/Hinglish phrases, some recorded with background noise.
- **Usability test:** hand the app to 3 people over 50 (family members) and time "log a reading" without instructions.
- **Offline test:** airplane-mode scenarios.
- Publish the results table in the PPT.

---

## 18. Risks & Mitigations
| Risk | Mitigation |
|---|---|
| Handwriting OCR inaccuracy | Confidence flags, manual edit, honest accuracy reporting |
| Drug dataset incomplete | Start with a focused, high-quality set; "unknown" state; state the dataset scope in the PPT |
| API rate limits during demo | Cache sample results; pre-recorded fallback inputs; retry logic |
| WhatsApp alerts need a human tap | Be honest: MVP uses prefilled WhatsApp/SMS links; production would use the WhatsApp Business API or FCM |
| Time overrun | Strict P0/P1/P2 cuts, as in section 16 |

---

## 19. PPT Outline (8–10 slides)
1. Title + team + tagline ("Aapki sehat ka saathi, 2 AM par bhi")
2. Problem (Sunita's story with 2 key stats you can source)
3. Solution: one connected loop (diagram)
4. How it works step by step (screenshots: scan → check → schedule → log → alert)
5. Tech architecture: "AI sees, rules decide"
6. Safety & trust (confidence flags, no diagnosis, privacy)
7. Results: accuracy tables from your tests
8. Why it's easy: the 10-second test, voice and Hindi
9. Impact, limitations (honest) & roadmap
10. Links: GitHub, live demo, video, QR codes

---

## 20. Future Scope
Bluetooth glucometer/BP integration, more regional languages, pharmacy price APIs, ABHA/ABDM integration, doctor portal, medicine-expiry/stock tracking, mental-health check-ins, CGM data, federated clinic dashboards.

---

*Disclaimer for README and PPT: SehatSaathi is an informational support tool built for a hackathon. It does not provide medical diagnosis or treatment and must not replace professional medical advice.*
