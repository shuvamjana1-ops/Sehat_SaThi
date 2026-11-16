# SehatSaathi — Real Android Phone Manual QA Checklist (16 Items)

Because automated JVM/Robolectric tests and headless browser checks cannot verify physical camera optics, real room acoustics, Android system TTS audio output, or third-party app handoffs (WhatsApp / Carrier SMS / Phone Dialer), complete this tickable checklist on a physical Android phone after installing `docs/SehatSaathi-debug.apk`.

---

## Top 10 Priority Phone Checks (Plus 6 Extended Checks)

- [ ] **1. Install & Cold Launch (`SehatSaathi-debug.apk`):** Install the APK on an Android phone (Android 8.0+), launch from the custom adaptive launcher icon, and confirm the Landing Screen loads in `< 2 seconds` with zero crashes.
- [ ] **2. 1-Tap Demo Mode & Guided Tour (No Internet / No API Key):** Enable Airplane Mode, tap **"Try demo"**, step through the 3-step guided tour, and verify Sunita Devi's 4 medicines, 30 days of readings, and 2 caregivers load completely offline.
- [ ] **3. Real Camera — Prescription Scan (`ScanRxScreen`):** Disable Airplane Mode, open **Medicines $\rightarrow$ Scan Prescription $\rightarrow$ Take photo**, grant Camera permission, photograph a real or printed prescription, and verify that extracted fields require confirmation and any low-confidence field (`< 0.70`) shows **"Please verify"**.
- [ ] **4. Real Camera — Glucometer / BP Monitor Photo (`LogReadingScreen`):** Open **Log $\rightarrow$ Photo**, photograph a glucometer or BP monitor screen, and verify the **"Please confirm before saving"** card appears before saving.
- [ ] **5. Real Microphone — Spoken Hindi & English Vitals (`LogReadingScreen`):** Open **Log $\rightarrow$ Voice**, grant Microphone permission, speak *"Aaj sugar 240 hai"* and *"BP 150 by 95"* in a quiet room and a noisy room, and verify the extracted reading requires confirmation before saving.
- [ ] **6. System Text-to-Speech Read-Aloud (`ScheduleSpeechHelper`):** Tap **"Listen"** on Today's schedule and on a Reading Result card; verify clear audio playback through the phone speaker and immediate stop when navigating away.
- [ ] **7. Emergency Screen Layout & Native Dialer (`112` & Family Call):** Trigger the **"Low sugar at 2 AM"** demo scenario (`55 mg/dL`), verify the solid red `#991515` **Emergency Screen** shows **Call 112** and **Call Rajesh** without scrolling, and tap **Call 112** to verify the Android phone dialer opens with `112` pre-filled (without auto-placing the call).
- [ ] **8. Native WhatsApp & SMS Share Handoff:** On an Urgent/Emergency reading result, tap **"WhatsApp Rajesh (Demo)"** and verify WhatsApp (or the Android system share sheet if WhatsApp is not installed) opens with the pre-filled patient name, reading, time, and urgency.
- [ ] **9. 1-Page Doctor Summary PDF & Android Print Spooler:** Open **Trends $\rightarrow$ Doctor Summary**, tap **Export 1-Page PDF** and **Print**, and verify the generated PDF opens cleanly in an Android PDF viewer / Print preview on a single page.
- [ ] **10. Demo Logout, Personal Account Creation & Seamless Account Switching:** Open **Profile / Account**, tap **Log Out / Create New Account**, sign up with your own name (e.g., *"Aarav Patel"*), verify your name replaces *"Sunita Devi"* across Dashboard, Family, and Care Card, and tap **Switch to Demo Account** to verify 1-tap switching back and forth.

---

## Extended Hardware & OS Accessibility Checks (Items 11–16)

- [ ] **11. Android Large Font Scale (`200%` OS Font Size):** In Android Settings $\rightarrow$ Display $\rightarrow$ Font size & display size, set Font size to largest, reopen SehatSaathi, and verify no buttons or status chips clip or overlap.
- [ ] **12. Android TalkBack Screen Reader Walkthrough:** Enable TalkBack, swipe through Today, Log, Medicines, and Emergency screens, and verify every icon button announces a meaningful `contentDescription` and status chips announce both icon and label.
- [ ] **13. System Reduced Motion / Animator Duration Scale Off:** In Developer Options, set Animator duration scale to `Animation off` (or enable Simple Mode), and verify all continuous animations stop immediately.
- [ ] **14. Offline AI Queue & Auto-Resume:** In Airplane Mode, queue a prescription scan, verify the *"Saved in offline queue"* banner appears, then re-enable Wi-Fi/5G and verify the queue processes automatically.
- [ ] **15. Caregiver Role Toggle (`FamilyScreen`):** Open **Family**, toggle between **Patient View** and **Caregiver View**, and verify the switch card never clips text and updates the linked patient summary immediately.
- [ ] **16. Account Deletion & Cache Wipe:** In **Profile $\rightarrow$ Privacy / Account**, type `DELETE` to confirm account deletion, and verify all local preferences and cached PDF/JSON exports in `cacheDir/exports` are wiped.
