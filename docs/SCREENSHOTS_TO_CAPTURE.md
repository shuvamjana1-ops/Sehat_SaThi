# SehatSaathi — Screenshots & Visual Evidence Inventory

All automated SVG visual evidence snapshots are stored in [`docs/perfection/screenshots/`](./perfection/screenshots/), and high-resolution handset screenshots are stored in `public/assets/aistudio/`.

---

## 1. Captured & Bundled Visual Evidence

| # | Screen / Flow | File Path | What It Demonstrates |
| :-: | :--- | :--- | :--- |
| 1 | **Landing & 3-Step Judge Quick Start** | `docs/perfection/screenshots/01_landing_quickstart.svg` | Deep Ocean `#0B4F6C` + Clinical Blue `#0E6E8C` palette, Manrope typography, 3-step Quick Start for Judges, and 1-tap Demo Scenario launcher. |
| 2 | **Log Reading & "Why This Result" Rule** | `docs/perfection/screenshots/02_log_reading_why_rule.svg` | 56dp keypad, human confirmation gate, deterministic `RiskEngine` output (`250 mg/dL -> Caution`), and the exact `"Why this result"` rule box. |
| 3 | **Solid Opaque Emergency Screen (112 SOS)** | `docs/perfection/screenshots/03_emergency_screen.svg` | Solid `#991515` background, zero floating widgets, `Call 112` + `Call Family` above the fold without scrolling, and non-prescriptive first-aid steps. |
| 4 | **Family & Caregiver Role Toggle** | `docs/perfection/screenshots/04_family_caregiver_toggle.svg` | Fixed high-contrast Patient View / Caregiver View pill + switch card (`FamilyScreen.kt`), linked patient summary, and persistent safety disclaimer footer. |
| 5 | **Account Switching (Demo <-> Personal)** | `docs/perfection/screenshots/05_account_switching.svg` | Seamless sign-out from Demo Mode, personal account creation, and 1-tap switching between Sunita Devi (Demo) and saved Personal Account. |
| 6 | **Web Companion & APK Download Notice** | `public/assets/aistudio/Screenshot_20261116_101811_Chrome_5fef3c57.jpg` | Live mobile Chrome render of `https://shuvamjana1-ops.github.io/Sehat_SaThi/` in dark mode with interactive phone preview. |

---

## 2. Recommended Live Screenshots to Capture on Physical Android Phone (Optional Judge Deck Extras)

If capturing additional live camera/handset photos on a physical Android phone before presentation:

1. **Physical Camera on Handwritten Prescription:** Open **Medicines $\rightarrow$ Scan Prescription $\rightarrow$ Take photo**, point at a printed/handwritten Indian prescription, and capture the **"Please verify"** confirmation table.
2. **Physical Camera on Glucometer LCD:** Open **Log $\rightarrow$ Photo**, photograph a real glucometer showing a reading, and capture the **"Please confirm before saving"** card.
3. **WhatsApp Pre-Filled Family Alert Handoff:** Trigger **"Low sugar at 2 AM"** demo scenario, tap **WhatsApp Rajesh (Demo)**, and capture the pre-filled message inside WhatsApp.
4. **Native Android Print / PDF Preview:** Open **Trends $\rightarrow$ Doctor Summary $\rightarrow$ Print / Export 1-Page PDF** and capture the Android system print preview of the 1-page clinical summary.
