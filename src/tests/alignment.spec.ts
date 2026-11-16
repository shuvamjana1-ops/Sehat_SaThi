/**
 * Reference Playwright Alignment Detector specification (src/tests/alignment.spec.ts)
 * Mirrors the Robolectric Compose AlignmentDetectorTest in app/src/test/java/com/example/AlignmentDetectorTest.kt
 * across viewports: 320x568, 360x800, 390x844, 768x1024, 1024x768, 1440x900, and 800x360.
 */

export const VIEWPORTS = [
  { width: 320, height: 568, label: 'compact-phone-320' },
  { width: 360, height: 800, label: 'android-phone-360' },
  { width: 390, height: 844, label: 'phone-390' },
  { width: 768, height: 1024, label: 'tablet-portrait-768' },
  { width: 1024, height: 768, label: 'tablet-landscape-1024' },
  { width: 1440, height: 900, label: 'desktop-1440' },
  { width: 800, height: 360, label: 'landscape-phone-800x360' },
] as const;

export const ROUTES = [
  '/',
  '/login',
  '/signup',
  '/forgot-password',
  '/dashboard',
  '/log-reading',
  '/scan-rx',
  '/medicines',
  '/trends',
  '/family',
  '/account#overview',
  '/account#health',
  '/account#medicines',
  '/account#activity',
  '/account#privacy',
  '/info/safety',
] as const;

export interface AlignmentFinding {
  route: string;
  viewport: string;
  mode: 'light' | 'dark' | 'simple';
  category:
    | 'overflow'
    | 'overlap'
    | 'wrapping'
    | 'vertical-alignment'
    | 'horizontal-alignment'
    | 'grid-card-alignment'
    | 'divider-alignment'
    | 'typography';
  severity: 'High' | 'Medium' | 'Low';
  selector: string;
  detail: string;
}
