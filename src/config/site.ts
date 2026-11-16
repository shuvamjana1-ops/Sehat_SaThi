/**
 * Site configuration and verified non-invented metadata for SehatSaathi.
 * Optional contact fields stay empty until real contact details are provided.
 */
export const SITE_CONFIG = {
  APP_NAME: 'SehatSaathi',
  EMERGENCY_NUMBER: '112',
  LAST_UPDATED: 'November 16, 2026',
  GITHUB_URL: 'https://github.com/shuvamjana1-ops/Sehat_SaThi',
  APK_DOWNLOAD_URL: './docs/SehatSaathi-debug.apk',
  // Leave blank until real contact details are supplied (hidden automatically when empty).
  SUPPORT_PHONE: '',
  SUPPORT_EMAIL: '',
  SUPPORT_ADDRESS: '',
} as const;

export const REAL_BUILD_METRICS = {
  MEDICINE_COUNT: 77,
  INTERACTION_COUNT: 30,
  DETERMINISTIC_CHECK_COUNT: 60,
  AUTOMATED_UNIT_TEST_COUNT: 60,
} as const;
