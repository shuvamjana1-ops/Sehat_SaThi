#!/usr/bin/env node
/**
 * Unified QA Runner for SehatSaathi (`npm run qa`)
 * 1. Runs `scripts/evaluate.mjs` to verify deterministic clinical boundaries & generate `docs/ACCURACY_REPORT.md`
 * 2. Runs `gradle :app:testDebugUnitTest` to execute all JUnit & Robolectric test suites
 */
import { execSync } from 'node:child_process';

console.log('=== [1/2] Running Deterministic Clinical Safety Evaluator ===');
execSync('node scripts/evaluate.mjs', { stdio: 'inherit' });

console.log('=== [2/2] Running Android JUnit & Robolectric Test Suite ===');
execSync('gradle :app:testDebugUnitTest', { stdio: 'inherit' });

console.log('=== QA PASS: All deterministic and unit/Robolectric suites succeeded ===');
