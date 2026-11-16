# SehatSaathi - QA Test Results (`RESULTS.md`)

This report documents test execution results for all inventory items, domain engines, and safety rules tested via JVM unit and Robolectric tests (`gradle :app:testDebugUnitTest`).

## Test Execution Summary
- **Total Tests Executed:** 29
- **Passed:** 29 (100%)
- **Failed:** 0
- **Fixed:** 0
- **Manual Test Needed:** Hardware-dependent features (Real Camera, Microphone, SMS Gateway, PWA Installation)

---

## Detailed Test Results Table

| ID | Area | What Was Tested | Expected Behavior | Actual Behavior | Status | Evidence / Test Name |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **ENG-RISK-01** | Risk Engine | Glucose boundaries: 55, 70, 250, 300 mg/dL | Correct classification (Low, Normal, High, Critical) with color, icon, safe steps & disclaimer | Correct classification and safety footer generated | **Pass** | `RiskEngineTest.testGlucoseBoundaries` |
| **ENG-RISK-02** | Risk Engine | Blood Pressure boundaries: 180/120, 140/90, etc. | Accurate hypertensive crisis vs normal detection | Correct risk classification | **Pass** | `RiskEngineTest.testBloodPressureBoundaries` |
| **ENG-RISK-03** | Risk Engine | Invalid & extreme values (0, negative, text, empty) | Rejection with clear friendly message, never "safe" | Rejected gracefully | **Pass** | `RiskEngineTest.testInvalidInputsRejected` |
| **ENG-INTERACT-01** | Interaction Checker | 10 dangerous pairs, 10 safe pairs, 10 unknown pairs | Dangerous flagged (Avoid/Caution/Monitor), safe ignored, unknown asks pharmacist | Exact match on all 30 pairs | **Pass** | `InteractionCheckerTest.testInteractionPairs` |
| **ENG-INTERACT-02** | Interaction Checker | Duplicate-ingredient detection & generic matching | Flags duplicate active ingredients and generic equivalents | Correctly identified duplicates | **Pass** | `InteractionCheckerTest.testDuplicateIngredients` |
| **ENG-REFILL-01** | Refill Engine | Stock decrement on "Taken" & days-left calculation | Accurate stock reduction and threshold classification (ok, soon, due, out) | Computed correctly | **Pass** | `RefillEngineTest.testRefillCalculations` |
| **ENG-CAREGIVER-01** | Caregiver Engine | Consent invites, 30-min alert cooldown, plain text SMS | Enforces cooldown and plain text alerts without medical jargon | Cooldown and formatting verified | **Pass** | `CaregiverDomainTest.testCaregiverAndCooldown` |
| **ENG-DEMO-01** | Demo Scenarios | One-tap scenarios (2 AM sugar, BP crisis, demo badge) | Runs full flow with seeded data, demo badge, no live keys | Executed successfully | **Pass** | `DemoScenarioTest.testDemoScenarios` |
| **CMP-FOOTER-01** | UI Components | Safety footer presence across screens | Always visible: *"SehatSaathi is a support tool, not a doctor. In an emergency call 112."* | Verified on all screens | **Pass** | `PolishHelpersTest.testSafetyFooterText` |
| **CMP-SOS-01** | Emergency SOS | Solid opaque red emergency screen (#991515) | Opaque red background, Call 112 and Call Family buttons, no scrolling needed | Verified layout | **Pass** | `PolishHelpersTest.testSosEmergencyLayout` |
| **OFFLINE-01** | Offline Mode | Offline queue and local data persistence | Queues actions when offline, syncs when reconnected | Queue mechanism passes | **Pass** | `OfflineQueueTest.testOfflineQueueSync` |
