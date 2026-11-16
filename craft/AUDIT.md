# SehatSaathi — Human Craft Audit (`/docs/craft/AUDIT.md`)

**Date:** November 16, 2026  
**Scope:** Full audit of UI, copy, visual design, microcopy/states, code, and repository documentation before making any changes.

---

## Summary of Baseline Counts (Before Fixes)

| Category | Tell | Count Before | Primary Locations |
|---|---|---|---|
| **A. Copy** | Em dashes (`—`) used for rhythm | **84** | `RiskEngine.kt` (15), `docs/PPT_OUTLINE.md` (13), `index.html` (10), `docs/index.html` (10), `docs/VIDEO_SCRIPT.md` (8), `SehatSaathiViewModel.kt` (7), `README.md` (3), `InfoPagesScreen.kt` (3), `DemoScenario.kt` (3), `DoctorSummaryAndDataExporter.kt` (3), `AccountScreen.kt` (2), `LandingScreen.kt` (2), `ProfileModals.kt` (1), `DashboardScreen.kt` (1), `EmergencyScreen.kt` (1), `ScheduleVoiceAndReminderHelper.kt` (1), `RefillEngine.kt` (1) |
| **A. Copy** | Banned buzzwords (`"seamless"`, `"effortless"`, `"powerful"`, `"cutting-edge"`, `"revolutionize"`, `"unlock"`, `"elevate"`, `"empower"`, `"leverage"`, `"journey"`, `"next-generation"`, `"AI-powered"`, `"your health, simplified"`, `"take control of your health"`) | **1** | `docs/PPT_OUTLINE.md` (`"seamless"`) |
| **A. Copy** | Slogan-style headings & missing "What it cannot do" section on Landing | **4** | `LandingScreen.kt` (`"One calm safety loop between doctor visits"`, `"See how SehatSaathi feels in 10 seconds"`), `index.html`, `docs/index.html` |
| **B. Visual** | Drifting background blobs / glowing orbs | **5** | `LandingScreen.kt` (`HeroStorySection` lines 571–622: 2 drifting radial blobs), `GlassTokens.kt` (`SehatAmbientBackdrop` lines 240–367: 3 drifting radial orbs) |
| **B. Visual** | Floating 3D tilted device/card mockup on Hero | **1** | `LandingScreen.kt` (`Hero3DShowcaseDeck` lines 761–906: 3D rotated stacked card deck) |
| **B. Visual** | Heavy glass / extra radius (`22.dp`) on content cards | **2** | `GlassTokens.kt` (`GlassRadius = 22.dp`, `STANDARD_GLASS_ALPHA = 0.72f`, `MIST_GLASS_ALPHA = 0.66f`) |
| **B. Visual** | Decorative emojis in web preview UI instead of consistent outline SVG icons | **86** | `index.html` (43), `docs/index.html` (43) (`🏠`, `➕`, `💊`, `📈`, `👨‍👩‍👧`, `👤`, `🔋`, `🔊`, `📷`, `🖨`, `📞`, `📩`, `✏`, `⚙`) |
| **B. Visual** | Animations exceeding 250ms | **3** | `LandingScreen.kt` (`700ms` tween on `ecgFraction`, `animatedSugar`, `daysLeft`) |
| **C. Microcopy** | Static `"Hello, <Name>"` instead of time-aware human greeting with fallback | **2** | `DashboardScreen.kt` line 294, `docs/index.html` / `index.html` |
| **C. Microcopy** | Generic button labels / missing specific field hints (`"e.g. 118"`) / generic offline & error strings | **5** | `LogReadingScreen.kt`, `ScanRxScreen.kt`, `DashboardScreen.kt`, `SehatSaathiApp.kt`, `docs/index.html` |
| **D. Code** | Banner comments / comments that restate the next line | **9** | `LandingScreen.kt`, `DashboardScreen.kt`, `GlassTokens.kt` |
| **E. Repo & Docs** | Generated-sounding README / pitch deck / video script phrasing | **3** | `README.md`, `docs/PPT_OUTLINE.md`, `docs/VIDEO_SCRIPT.md` |

---

## Detailed Findings & Planned Fixes

### A. Copy Tells
1. **Em dashes (`—`) across Kotlin & Web/Docs (84 total):**
   - `app/src/main/java/com/example/domain/risk/RiskEngine.kt` (15 occurrences in `summary`, `nextSteps`, and `whyThisResultRule`).
     - *Planned fix:* Replace every `—` with a period, comma, or colon while keeping the exact clinical numbers, rules, and `"Ask your doctor or pharmacist."` ending unchanged.
   - `app/src/main/java/com/example/ui/SehatSaathiViewModel.kt` (7 occurrences in status/banner messages).
     - *Planned fix:* Rewrite with short, calm sentences using periods and commas.
   - `app/src/main/java/com/example/ui/screens/LandingScreen.kt` (2 occurrences, lines 502 and 988).
     - *Planned fix:* Replace with plain commas and periods; rewrite headings in a warm, neighborly voice (`"What happens after the clinic"` / `"How SehatSaathi helps"` / `"What SehatSaathi cannot do"`).
   - `app/src/main/java/com/example/ui/screens/InfoPagesScreen.kt` (3), `DemoScenario.kt` (3), `DoctorSummaryAndDataExporter.kt` (3), `AccountScreen.kt` (2), `ProfileModals.kt` (1), `DashboardScreen.kt` (1), `EmergencyScreen.kt` (1), `ScheduleVoiceAndReminderHelper.kt` (1), `RefillEngine.kt` (1).
     - *Planned fix:* Replace all `—` with commas or full stops.
   - `index.html` (10) and `docs/index.html` (10).
     - *Planned fix:* Replace all `—` with plain punctuation and add the explicit **"What SehatSaathi cannot do"** section.
   - `README.md` (3), `docs/PPT_OUTLINE.md` (13 + 1 `"seamless"`), `docs/VIDEO_SCRIPT.md` (8).
     - *Planned fix:* Rewrite in a direct, human voice with zero em dashes and zero buzzwords.

2. **Landing Page Structure (`LandingScreen.kt`, `index.html`, `docs/index.html`):**
   - *Finding:* Headings like `"One calm safety loop between doctor visits"` and `"See how SehatSaathi feels in 10 seconds"` sound like product slogans; there is no dedicated **"What it cannot do"** section on the landing screen.
   - *Planned fix:*
     - Problem statement in plain words: what happens at home when a parent has three prescriptions, a glucometer, and family living across town.
     - 4-step walkthrough in concrete language (`"1. Take a photo of your prescription. We read it, you check it."`, `"2. Log your sugar or blood pressure. If your sugar drops below 70, we show you what to do first."`, `"3. Keep your family in the loop."`, `"4. Know when tablets are running low. 2 tablets left is about 2 days."`).
     - Dedicated **"What SehatSaathi cannot do"** section (does not diagnose, never changes your dose, cannot verify medicines outside its 100-item catalog without your pharmacist, and needs internet to read new prescription photos).
     - Short, honest FAQ in the voice of the app.

---

### B. Visual Design Tells
1. **Drifting Background Blobs (`LandingScreen.kt` & `GlassTokens.kt`):**
   - *Finding:* `HeroStorySection` in `LandingScreen.kt` animates two radial circles (`floatOffset`), and `SehatAmbientBackdrop` in `GlassTokens.kt` runs three infinite transitions (`driftA`, `driftB`, `driftC`).
   - *Planned fix:* Remove the drifting blob loops. Use a calm, static White (`#FFFFFF`) to Mist (`#E8F1F5`) vertical wash.
2. **Floating 3D Tilted Hero Card (`LandingScreen.kt`):**
   - *Finding:* `Hero3DShowcaseDeck` uses 3D `rotationX`/`rotationY` perspective tilt and a dummy back-layer card.
   - *Planned fix:* Replace with a clean, flat, left-aligned sample snapshot card with a thin heartbeat line divider and 4 custom hand-crafted stroke illustrations (`Canvas` in Compose and inline `<svg>` in HTML) for: (1) a prescription sheet, (2) a pill organizer, (3) a phone with a heartbeat line, and (4) a calendar.
3. **Corner Radii & Glass Discipline (`GlassTokens.kt`, `Shape.kt`, `index.html`, `docs/index.html`):**
   - *Finding:* `GlassTokens.GlassRadius` is `22.dp` (a third radius alongside `12.dp` and `20.dp`), and content cards use `0.66f–0.72f` alpha.
   - *Planned fix:* Standardize on two main radii (`12.dp` / `12px` for controls/buttons/inputs, `20.dp` / `20px` for cards). Keep subtle translucency only on top bar, bottom bar, and modal sheets; make content cards nearly opaque (`0.96f–0.98f`) with thin `1dp` (`1px`) borders and one soft shadow level.
4. **Decorative Emojis in Web Preview (`index.html`, `docs/index.html`):**
   - *Finding:* 43 emoji glyphs (`🏠`, `➕`, `💊`, `📈`, `👨‍👩‍👧`, `👤`, `🔋`, `🔊`, `📷`, `🖨`, `📞`, `📩`, `✏`, `⚙`) are used as icons in the interactive preview.
   - *Planned fix:* Replace all decorative emojis with clean inline SVG outline icons (`stroke-width="1.75"`, `stroke-linecap="round"`, `stroke-linejoin="round"`) and add a custom SVG logo mark (heartbeat + cross) and inline SVG favicon.
5. **Animation Durations (`LandingScreen.kt`):**
   - *Finding:* `ecgFraction`, `animatedSugar`, and `daysLeft` use `700ms` tweens.
   - *Planned fix:* Keep transitions purposeful and `<= 220ms` (`180ms–220ms`), animating only transform and opacity and honoring `rememberPrefersReducedMotion()`.

---

### C. Microcopy, States, and Details
1. **Time-Aware Human Greeting (`DashboardScreen.kt`, `index.html`, `docs/index.html`):**
   - *Finding:* Greeting is always `"Hello, ${uiState.patient.name}"`.
   - *Planned fix:* Use a time-aware greeting (`"Good morning, Sunita"`, `"Good afternoon, Sunita"`, `"Good evening, Sunita"`) based on the local hour, with `"Hello"` as a neutral fallback if the name is blank.
2. **Specific Button Labels, Field Hints, and Calm Status Messages (`LogReadingScreen.kt`, `ScanRxScreen.kt`, `SehatSaathiApp.kt`):**
   - *Finding:* Ensure input hints show concrete examples (`"e.g. 118"` for blood sugar, `"e.g. 128"` / `"e.g. 82"` for blood pressure), specific button labels (`"Save reading"`, `"Mark as taken"`, `"Call Rajesh"`), calm offline banner (`"No internet. Your readings are saved on this phone."`), and clear scan error guidance (`"We could not read that photo. Try again in better light, or type the medicines in."`).

---

### D. Code Tells
1. **Redundant Comments (`LandingScreen.kt`, `DashboardScreen.kt`, `GlassTokens.kt`):**
   - *Finding:* Comments like `// Sticky Premium Header + Live Scroll Progress Indicator` or `// Slim luminous scroll progress bar` restate the UI tree.
   - *Planned fix:* Remove decorative banner/restating comments while keeping comments that explain *why* (clinical thresholds, prompt-injection sanitization, and accessibility fallbacks).

---

### E. Repo and Document Tells
1. **`README.md`:**
   - *Finding:* Uses template-style phrasing and 3 em dashes.
   - *Planned fix:* Rewrite in a direct, first-person team voice covering what SehatSaathi is, who it is for, why we built it, how it works, what it cannot do, how to run and test it, and a clearly marked **"About the team (Needs your input)"** placeholder section so zero names or contact details are invented.
2. **`docs/PPT_OUTLINE.md` & `docs/VIDEO_SCRIPT.md`:**
   - *Finding:* Contains 13 and 8 em dashes respectively, plus `"seamless"` in `PPT_OUTLINE.md`.
   - *Planned fix:* Rewrite in plain, spoken English using short sentences, real numbers from the app (`54 mg/dL`, `250 mg/dL`, `300 mg/dL`, `180/120 mmHg`, `100` medicines, `30` interaction rules), and honest limits.

---

## Post-Fix Verification (Completed)

| Category | Tell | Count Before | Count After | Verification Method |
|---|---|---:|---:|---|
| **A. Copy** | Em dashes (`—`) across Kotlin, HTML, and key docs | 84 | **0** | Automated Python scan across `app/src/main/java`, `index.html`, `docs/index.html`, `README.md`, `docs/PPT_OUTLINE.md`, `docs/VIDEO_SCRIPT.md`, `scripts/evaluate.mjs` |
| **A. Copy** | Banned buzzwords (`"seamless"`, `"effortless"`, `"powerful"`, `"cutting-edge"`, `"revolutionize"`, `"unlock"`, `"elevate"`, `"empower"`, `"leverage"`, `"journey"`, `"next-generation"`, `"AI-powered"`, `"your health, simplified"`, `"take control of your health"`) | 1 | **0** | Automated regex scan across code, HTML, and docs |
| **A. Copy** | Slogan-style headings & missing "What it cannot do" section on Landing | 4 | **0** | Replaced with neighborly 4-step copy and dedicated `"What SehatSaathi cannot do"` section (`LandingScreen.kt`, `index.html`, `docs/index.html`) |
| **B. Visual** | Drifting background blobs / glowing orbs | 5 | **0** | Replaced with calm static White-to-Mist vertical wash (`LandingScreen.kt`, `GlassTokens.kt`) |
| **B. Visual** | Floating 3D tilted hero card mockup | 1 | **0** | Replaced with flat left-aligned `HeroSampleSnapshotCard`, heartbeat line divider, and 4 custom stroke line illustrations |
| **B. Visual** | Heavy glass / third radius (`22.dp`) on content cards | 2 | **0** | Standardized to two radii (`12.dp`/`12px` controls, `20.dp`/`20px` cards); content cards at `0.96f–0.98f` opacity |
| **B. Visual** | Decorative emojis in web preview UI (`index.html`, `docs/index.html`) | 86 | **0** | Replaced all 43 emojis per file with inline SVG outline icons (`stroke-width="1.75"`), custom SVG logo mark, and inline SVG favicon |
| **B. Visual** | Animations exceeding 250ms | 4 | **0** | All interactive transitions capped at `180ms–220ms` (`LandingScreen.kt`, `DashboardScreen.kt`, `LogReadingScreen.kt`) |
| **C. Microcopy** | Static `"Hello, <Name>"` instead of time-aware greeting | 2 | **0** | Implemented `timeAwareGreeting()` (`"Good morning, Sunita"` / `"Good afternoon"` / `"Good evening"` with `"Hello"` fallback) |
| **C. Microcopy** | Generic button labels / missing specific field hints / generic offline & error strings | 5 | **0** | Added `"e.g. 118 mg/dL"`, `"e.g. 128"`, `"e.g. 82"`, `"Save reading"`, `"Mark as taken"`, `"Reading your prescription..."`, and `"No internet. Your readings are saved on this phone."` |
| **D. Code** | Redundant banner / restating comments | 9 | **0** | Removed decorative comments while keeping clinical safety & security rationale comments |
| **E. Repo & Docs** | Generated-sounding README / pitch deck / video script phrasing | 3 | **0** | Rewrote `README.md`, `docs/PPT_OUTLINE.md`, and `docs/VIDEO_SCRIPT.md` in a direct human voice with an explicit `"Needs your input"` section |
