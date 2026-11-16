# SehatSaathi - QA & Security Inventory

This document inventories every screen, route, component, domain engine, state machine, and security boundary in the SehatSaathi Android application.

## 1. Screens & Navigation Routes
- **ID: SCR-LANDING** - Landing / Welcome Screen: Hero banner, feature highlights, FAQ accordion, footer safety disclaimer, "Try a demo scenario" panel, and "Get Started" / Sign-in action.
- **ID: SCR-DASHBOARD** - Main Dashboard: Vitals summary cards (Glucose, BP, Heart Rate, Cholesterol), Quick Jump shortcuts, active medications reminder widget, demo scenario panel (in Demo mode), and bottom/rail navigation.
- **ID: SCR-LOGGING** - Vitals Logging Screen: Keypad entry, voice input toggle (mocked/audio), photo/ocr logging, context tags, and deterministic risk evaluation.
- **ID: SCR-SCANNER** - Prescription Scanner Screen: Image capture / picker, OCR extraction with confidence chips, interaction checker, duplicate-ingredient detector, generic suggestions, pharmacist card, and schedule generator.
- **ID: SCR-REFILLS** - Refills & Stock Screen: Medication stock cards (Days left: OK, Soon, Due, Out), refilled action, snooze, adjust stock, pharmacy tel link, WhatsApp link, and family notification link.
- **ID: SCR-CAREGIVER** - Caregiver Dashboard Screen: Linked patients list with status chips (Green/Amber/Red), vital summaries, access verification controls, missed-dose escalation logs, and weekly digest preview/export.
- **ID: SCR-SIMPLE_MODE** - Simple Mode Screen: Elderly-friendly layout with 4 large touch targets (Voice Log, Refills, Emergency SOS, Daily Check-in), large typography, and spoken confirmation hooks.
- **ID: SCR-SOS** - Emergency SOS Screen: Solid opaque red background (#991515), Call 112, Call Family, and Safe Steps guidance without scrolling at standard viewports (360x800).
- **ID: SCR-SAFETY** - Safety & Dataset Coverage Screen: Dataset coverage stats (100 medicines, 30+ interaction pairs), accuracy report links, public verification sources, and safety policy notices.
- **ID: SCR-SETTINGS** - Settings & Profile Screen: Account management, theme toggle (Light, Dark, System), data export, delete account, and consent management.

## 2. Domain Engines & Business Logic
- **ID: ENG-RISK** - Deterministic Risk Engine: Evaluates blood glucose boundaries (55, 70, 250, 300 mg/dL) and Blood Pressure boundaries (systolic 140/180, diastolic 90/120) with deterministic color coding (Green/Amber/Red), icons, text labels, safe-step guidance, and mandatory footer notice *"Ask your doctor or pharmacist."*
- **ID: ENG-INTERACTION** - Interaction Checker: Validates 10 seeded dangerous pairs (Avoid/Caution/Monitor), 10 safe pairs, and 10 unknown pairs ("Not in our database. Please ask your pharmacist."), plus duplicate-ingredient grouping and generic matching.
- **ID: ENG-REFILL** - Refill Engine: Decrements stock on "Taken" action, computes days-left, triggers threshold alerts (ok, soon, due, out), supports snooze, cooldown, quiet hours, and pause.
- **ID: ENG-CAREGIVER** - Caregiver & Alert Delivery Engine: Validates patient-caregiver consent invites, applies 30-minute alert cooldowns, formats concise plain-text SMS/WhatsApp messages, and tracks delivery status.
- **ID: ENG-DEMO** - Demo Scenario Engine: One-tap seeded fictional scenarios ("Low sugar at 2 AM", "High BP", "Sample prescription", "Medicine running out", "Missed dose") with Demo badges and reset capability.
- **ID: ENG-AI_PROXY** - Secure Gemini Proxy & OCR Pipeline: Validates request size limits, MIME types, rate limits, schema validation with retry, and fallback to manual entry on failure.

## 3. UI Components & Interactivity
- **ID: CMP-GLASS** - Glass Tokens & Ambient Backdrop: Frosted translucent surfaces (66%–72% cards, 90% scrims, 96%+ reduced-transparency fallback), specular borders, and drifting radial orbs.
- **ID: CMP-FOOTER** - Safety Footer: Permanent sticky footer displaying *"SehatSaathi is a support tool, not a doctor. In an emergency call 112."* across all screens.
- **ID: CMP-TOAST** - Toast & Snackbar System: Success/error messages with appropriate accessibility roles and non-blocking display.
- **ID: CMP-DIALOG** - Confirmation & Modal Dialogs: Focus trap, Escape key handling, scrim cancel, and safe option default focus.
- **ID: CMP-THEME** - Theme Toggle & Preferences: Light, Dark, System modes with instant persistence and no flash.

## 4. Security & Data Boundaries
- **ID: SEC-STORAGE** - Client Storage: Encrypted/secure local preferences, session token management, and complete clearing on logout / "delete my data".
- **ID: SEC-AUTH** - Authentication & Access Control: Protected route guards, Google Sign-in / Credential Manager integration, and caregiver access revocation.
- **ID: SEC-INPUT** - Input Sanitization: XSS defense, prompt injection neutralization for OCR/voice text, and URL parameter sanitization.
