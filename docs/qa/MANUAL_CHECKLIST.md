# SehatSaathi - Manual Device Test Checklist (`MANUAL_CHECKLIST.md`)

This checklist covers items that require physical Android hardware and cannot be fully validated in an automated headless testing environment.

## Physical Device Verification Checklist

- [ ] **App Installation & Launch:** Install the APK on a physical Android phone, verify launcher icon appearance, app name ("SehatSaathi"), and splash/launch speed.
- [ ] **Camera Prescription Scan:** Capture a real paper prescription using the device camera. Verify image compression, OCR extraction, confidence chips, interaction check, and schedule generation.
- [ ] **Microphone Voice Input:** Use voice input in a moderately noisy room. Verify transcription accuracy and safety confirmation step before saving.
- [ ] **Text-to-Speech (TTS):** Tap the read-aloud button on medication schedules and alerts. Verify volume, clarity, and language (English only).
- [ ] **Push Notifications & Reminders:** Set a medication reminder, leave the app in the background, and verify that the notification fires on time with correct plain-text wording.
- [ ] **SOS Location & Emergency Call:** Grant/deny location permission and test the SOS screen. Verify that tapping Call 112 or Call Family opens the dialer correctly without auto-sending messages.
- [ ] **Messaging Integration:** Tap "Notify family via WhatsApp/SMS" and verify that the external messaging app opens with the prefilled, concise plain-text message.
- [ ] **Offline Mode Transition:** Turn on Airplane Mode. Log a vital reading, verify that the offline banner appears, and confirm local persistence and sync upon reconnection.
- [ ] **Screen Rotation & Keyboard Layout:** Rotate device between portrait and landscape. Verify that forms remain usable, keyboard does not obscure inputs, and emergency screens remain non-scrollable.
- [ ] **Battery-Saver & Performance:** Enable battery saver mode. Verify smooth scrolling of Liquid Glass surfaces and responsive touch feedback.
