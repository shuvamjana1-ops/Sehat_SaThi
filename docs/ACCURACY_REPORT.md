# SehatSaathi Accuracy & Deterministic Evaluation Report

Generated: November 15, 2026  
Runtime Environment: Android JVM (Robolectric & JUnit Unit Test Suite)

## 1. Executive Summary

SehatSaathi relies on strict deterministic evaluation engines for clinical risk classification, drug interactions, refill schedules, and missed-dose escalations. Unverified AI models are never permitted to make clinical danger decisions or dosing recommendations.

| Evaluation Domain | Test Count | Pass Rate | Status / Notes |
| :--- | :--- | :--- | :--- |
| **Risk Engine Boundaries** | 12 | 100% | Tested at 69/70 (glucose), 53/54, 249/250, 299/300, 179/180 (BP) |
| **Interaction Pairs (30 pairs)** | 30 | 100% | 10 dangerous (Avoid), 10 caution (Monitor/Caution), 10 safe/unknown baseline |
| **Refill Status & Cooldowns** | 8 | 100% | Stock depletion, 30-minute alert cooldown, duplicate prevention |
| **Prescription OCR Parsing** | 15 fixtures | N/A* | *Not measured (Requires runtime Gemini API key)* |
| **Voice Transcript Parsing** | 20 fixtures | N/A* | *Not measured (Requires runtime Gemini API key)* |

---

## 2. Risk Engine Boundary Verification

Deterministic thresholds are evaluated against strict numeric boundaries to prevent boundary rounding discrepancies:
- **Blood Glucose Low (< 70 mg/dL):** 69 mg/dL correctly triggers `URGENT` low alert; 70 mg/dL triggers `NORMAL` or `CAUTION`.
- **Blood Glucose High Urgent (> 250 mg/dL):** 249 mg/dL triggers `CAUTION`; 250 mg/dL triggers `URGENT`.
- **Blood Pressure Crisis (Systolic >= 180 or Diastolic >= 120):** 179 mmHg triggers `CAUTION`; 180 mmHg triggers `URGENT` crisis protocol.

---

## 3. Drug Interaction Classification (30 Fixtures)

- **Dangerous (Avoid - 10 pairs):** e.g., Glimepiride + Fluconazole, Ibuprofen + Aspirin, Warfarin + Aspirin, Sildenafil + Nitroglycerin, Methotrexate + NSAIDs, Lithium + Diuretics, Potassium Chloride + Spironolactone, Ciprofloxacin + Tizanidine, Simvastatin + Gemfibrozil, Digoxin + Amiodarone.
- **Caution / Monitor (10 pairs):** e.g., Metformin + Ciprofloxacin, Atorvastatin + Azithromycin, Amlodipine + Simvastatin, Telmisartan + Spironolactone, Clopidogrel + Omeprazole, Ramipril + Ibuprofen, Enalapril + Diclofenac, Glimepiride + Ciprofloxacin, Metformin + Insulin Glargine, Levothyroxine + Calcium Carbonate.
- **Safe / Unknown Baseline (10 pairs):** e.g., Paracetamol + Pantoprazole, Amoxicillin + Paracetamol, Cetirizine + Paracetamol, Vitamin B12 + Metformin, Folic Acid + Telmisartan, Cholecalciferol + Atorvastatin, Montelukast + Cetirizine, Ondansetron + Paracetamol, Drotaverine + Pantoprazole, Zinc + Vitamin C.

All 30 pairs execute successfully against deterministic lookup tables.

---

## 4. Refill Status & Alert Cooldown
- **Refill Status Transitions:** Evaluated for `OK` (> 7 days left), `Refill soon` (4–7 days), `Refill due` (1–3 days), `Out of stock` (0 days), and `Unknown stock`.
- **Alert Cooldown:** Enforces a 30-minute rate limit between repeated urgent clinical notifications to prevent alert fatigue.
