# SehatSaathi: 2:45 Live Demo Video Script

- **Target time:** 2 minutes 45 seconds
- **Setup:** Android phone or emulator running `SehatSaathi-debug.apk` (with the browser preview open in a second tab if needed).

---

## 0:00 to 0:25 | The Problem and Our Rule (Landing Screen)
- **On screen:** SehatSaathi **Landing Screen** (`LandingScreen.kt`). Show Sunita Devi's sample snapshot card and the 4 steps.
- **Click path:**
  1. Open the app on `LandingScreen`.
  2. Scroll briefly to show **Four clear steps between clinic visits** and **What SehatSaathi cannot do**.
- **What to say:**
  > "When an older parent in India comes home from a 10-minute clinic visit, they are often managing four to eight tablets a day on their own. A brand switch on a handwritten prescription can cause accidental double-dosing of the same active salt, and a 2 AM low-sugar reading written in a notebook does not wake up the family. General AI chatbots are not safe here because they guess. In SehatSaathi, AI only reads and explains. Fixed clinical code makes every safety decision."

---

## 0:25 to 0:55 | Try Demo and Today's Schedule (Dashboard Screen)
- **Click path:**
  1. Tap **Try demo** (`hero_try_demo_btn`).
  2. Skip or step through the short tour (`tour_skip_btn`).
  3. On **Today** (`DashboardScreen`), tap **Listen** (`tts_schedule_btn`), then tap **Mark as taken** on a scheduled tablet (`dose_take_btn_*`).
  4. Toggle **Simple Mode** in the top bar (`topbar_simple_mode_btn`) on and off to show how the screen simplifies to three tabs.
- **What to say:**
  > "One tap on 'Try demo' opens 67-year-old Sunita Devi's sample profile with no sign-up or API key required. All text uses Manrope at 18sp or larger, with 56dp buttons. When Sunita marks a tablet as taken, her remaining tablet count drops by one dose. If she taps Skip, her tablet count stays the same, and the app never tells her to double her next dose."

---

## 0:55 to 1:35 | Logging a Reading and Emergency Mode (Log Reading Screen)
- **Click path:**
  1. Tap **+ Log** (`bottom_nav_log_reading`) in the bottom bar.
  2. Type **`250`** on the keypad, tap **Review & confirm** (`keypad_review_btn`), and tap **Save reading** (`confirm_save_reading_btn`). Point to the **Why this result** box.
  3. Next, tap the demo scenario button **Low sugar at 2 AM** (`demo_scenario_LOW_SUGAR_2AM`).
  4. The solid red **Emergency Screen** (`emergency_screen`) opens with **Call 112** and **Call Family** visible without scrolling. Tap **I am safe, return to SehatSaathi** (`close_emergency_btn`).
- **What to say:**
  > "Let's log a blood sugar reading of 250 mg/dL. Nothing is saved until we confirm it on the second step. Right below the result, 'Why this result' shows the exact rule that fired and reminds us to ask a doctor or pharmacist. Now let's trigger the 2 AM low-sugar scenario at 55 mg/dL. The solid red Emergency Screen opens immediately with Call 112 and Call Family right at the top, and prepares a WhatsApp message for her son."

---

## 1:35 to 2:10 | Prescription Scan, Duplicate Salts, and Generic Prices (Medicines Screen)
- **Click path:**
  1. Tap **Medicines** (`bottom_nav_medicines`), then tap **Scan prescription** (`open_scan_rx_btn`).
  2. Tap **Load sample prescription** (`load_sample_rx_btn`).
  3. Point to the **Please verify** badge on any low-confidence item, then tap **Confirm & check interactions** (`confirm_rx_btn`).
  4. Scroll on **Medicines** to show the duplicate-ingredient warning and the Jan Aushadhi generic price comparison.
- **What to say:**
  > "When we scan a prescription, any field where AI confidence is below 70 percent is marked 'Please verify' and cannot be saved until the user checks it. Once confirmed, our local 100-medicine Indian dataset flags duplicate active ingredients, checks 30 interaction rules, and shows Jan Aushadhi generic price ranges only when both the active ingredient and exact milligram strength match."

---

## 2:10 to 2:45 | Caregiver View, Doctor Summary PDF, and Account Switching
- **Click path:**
  1. Tap **Family** (`bottom_nav_family`) and switch on **Caregiver View** (`caregiver_role_switch_card`) to inspect Sunita Devi's summary and acknowledge an open alert.
  2. Tap **Trends** (`bottom_nav_trends`), open **Doctor Summary**, and tap **Export 1-Page PDF**.
  3. Tap the **Account** icon (`topbar_account_btn`) to show **Edit profile**, **Create New Account**, and **Switch to Demo Account**.
- **What to say:**
  > "In the Family tab, a son or daughter can switch to Caregiver View to check today's doses and alerts, or print a one-page PDF summary for the next clinic visit. And in the Account screen, judges can log out of Demo Mode to create their own profile and switch back to Sunita's demo in one tap. Every safety boundary is backed by 51 automated tests and 70 deterministic checks."
