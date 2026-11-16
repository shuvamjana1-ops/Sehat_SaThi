#!/usr/bin/env node
/**
 * Automated verification suite for the SehatSaathi home landing page
 * across /index.html and /docs/index.html.
 * Checks JS syntax, deterministic glucose rule boundaries, SEO/OG meta,
 * heading hierarchy, ARIA controls, internal/asset links, and copy hygiene.
 */
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import vm from 'node:vm';

const rootDir = process.cwd();
const siteTs = fs.readFileSync(path.join(rootDir, 'src', 'config', 'site.ts'), 'utf8');

function extractHtmlParts(html) {
  const scripts = [];
  let match;
  const scriptRe = /<script\b[^>]*>([\s\S]*?)<\/script>/gi;
  while ((match = scriptRe.exec(html)) !== null) {
    scripts.push(match[1]);
  }
  const stripped = html
    .replace(/<!DOCTYPE[^>]*>/gi, ' ')
    .replace(/<style\b[^>]*>[\s\S]*?<\/style>/gi, ' ')
    .replace(/<script\b[^>]*>[\s\S]*?<\/script>/gi, ' ')
    .replace(/<svg\b[^>]*>[\s\S]*?<\/svg>/gi, ' ')
    .replace(/<!--[\s\S]*?-->/g, ' ')
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/g, ' ')
    .replace(/&amp;/g, '&')
    .replace(/\s+/g, ' ')
    .trim();
  return { scripts, visibleText: stripped };
}

function checkHtmlFile(relPath, baseDir) {
  const fullPath = path.join(rootDir, relPath);
  const html = fs.readFileSync(fullPath, 'utf8');
  const { scripts, visibleText } = extractHtmlParts(html);
  let checks = 0;

  // 1. JavaScript syntax checks for every inline script
  assert.ok(scripts.length >= 2, `${relPath}: expected at least 2 inline scripts`);
  for (const [idx, code] of scripts.entries()) {
    new Function(code);
    assert.ok(code.length > 0, `${relPath}: script[${idx}] non-empty`);
    checks++;
  }

  // 2. Execute evaluateGlucoseRule directly in a clean sandbox
  const mainScript = scripts[scripts.length - 1];
  const fnMatch = mainScript.match(/function evaluateGlucoseRule\s*\([\s\S]*?\n\s{6}\}/);
  assert.ok(fnMatch, `${relPath}: evaluateGlucoseRule function not found`);
  const sandbox = {};
  vm.runInNewContext(`${fnMatch[0]}; this.evaluateGlucoseRule = evaluateGlucoseRule;`, sandbox);
  const evalRule = sandbox.evaluateGlucoseRule;

  const boundaries = [
    [53, 'EMERGENCY'],
    [54, 'URGENT'],
    [62, 'URGENT'],
    [69, 'URGENT'],
    [70, 'NORMAL'],
    [249, 'NORMAL'],
    [250, 'CAUTION'],
    [299, 'CAUTION'],
    [300, 'URGENT'],
    [-5, 'CAUTION'],
    [19, 'CAUTION'],
    [601, 'CAUTION'],
    [Number.NaN, 'CAUTION'],
  ];
  for (const [val, expectedLevel] of boundaries) {
    const res = evalRule(val);
    assert.equal(res.level, expectedLevel, `${relPath}: glucose ${val} expected ${expectedLevel}, got ${res.level}`);
    assert.ok(res.guidance.includes('Ask your doctor or pharmacist.'), `${relPath}: glucose ${val} guidance must end with doctor/pharmacist line`);
    checks++;
  }

  // 3. SEO, Meta, Font & No heavy libraries
  const descMatch = html.match(/<meta\s+name="description"\s+content="([^"]*)"/i);
  assert.ok(descMatch, `${relPath}: missing meta description`);
  assert.ok(descMatch[1].length > 40 && descMatch[1].length <= 155, `${relPath}: description length ${descMatch[1].length} out of bounds`);
  assert.ok(/<meta\s+property="og:title"/i.test(html), `${relPath}: missing og:title`);
  assert.ok(/<meta\s+property="og:description"/i.test(html), `${relPath}: missing og:description`);
  assert.ok(/<meta\s+property="og:type"/i.test(html), `${relPath}: missing og:type`);
  assert.ok(/<link\s+rel="canonical"/i.test(html), `${relPath}: missing canonical link`);
  assert.ok(/family=Manrope/i.test(html), `${relPath}: missing Manrope font`);
  assert.ok(!/three\.min\.js|particles\.js|<video\b/i.test(html), `${relPath}: forbidden heavy library or video tag found`);
  checks += 7;

  // 4. Heading hierarchy (exactly one h1, sequential h2 -> h3)
  const headingMatches = [...html.matchAll(/<(h[1-6])\b[^>]*>/gi)].map((m) => Number(m[1][1]));
  const h1Count = headingMatches.filter((n) => n === 1).length;
  assert.equal(h1Count, 1, `${relPath}: expected exactly 1 <h1>, found ${h1Count}`);
  let maxSeen = 0;
  for (const level of headingMatches) {
    if (level > maxSeen + 1 && maxSeen > 0) {
      assert.fail(`${relPath}: skipped heading level h${maxSeen} -> h${level}`);
    }
    if (level === 3) {
      assert.ok(headingMatches.includes(2), `${relPath}: <h3> used without preceding <h2>`);
    }
    if (level > maxSeen) maxSeen = level;
  }
  checks += 2;

  // 5. Landmarks, Drawer, and 7-item FAQ ARIA
  assert.ok(html.includes('class="skip-link"'), `${relPath}: missing skip-link`);
  assert.ok(html.includes('<header '), `${relPath}: missing header`);
  assert.ok(html.includes('<nav '), `${relPath}: missing nav`);
  assert.ok(html.includes('<main '), `${relPath}: missing main`);
  assert.ok(html.includes('<footer '), `${relPath}: missing footer`);
  assert.ok(html.includes('id="mobileDrawer" role="dialog" aria-modal="true"'), `${relPath}: mobile drawer ARIA missing`);
  const faqTriggers = [...html.matchAll(/class="faq-trigger"[^>]*aria-expanded="false"[^>]*aria-controls="([^"]+)"[^>]*id="([^"]+)"/g)];
  assert.equal(faqTriggers.length, 7, `${relPath}: expected 7 FAQ triggers, found ${faqTriggers.length}`);
  for (const [, panelId, btnId] of faqTriggers) {
    assert.ok(html.includes(`id="${panelId}" role="region" aria-labelledby="${btnId}"`), `${relPath}: FAQ panel ${panelId} ARIA mismatch`);
    checks++;
  }

  // 6. Internal #anchor targets & local file hrefs exist
  const idSet = new Set([...html.matchAll(/\sid="([^"]+)"/g)].map((m) => m[1]));
  const hrefs = [...html.matchAll(/\shref="([^"]+)"/g)].map((m) => m[1]);
  for (const href of hrefs) {
    if (href.startsWith('#')) {
      const targetId = href.slice(1);
      assert.ok(idSet.has(targetId), `${relPath}: broken anchor link #${targetId}`);
      checks++;
    } else if (href.startsWith('./') && !href.endsWith('/')) {
      const assetPath = path.join(baseDir, href.slice(2));
      assert.ok(fs.existsSync(assetPath), `${relPath}: broken relative link ${href} -> ${assetPath}`);
      checks++;
    }
  }

  // 7. Copy hygiene: zero em dashes, zero exclamation marks in visible text, zero buzzwords, zero emoji, required disclaimer
  assert.ok(!html.includes('—'), `${relPath}: contains forbidden em dash`);
  assert.ok(!visibleText.includes('!'), `${relPath}: visible body text contains exclamation mark`);
  const emojiRe = /[\u{1F300}-\u{1FAFF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}]/u;
  assert.ok(!emojiRe.test(visibleText), `${relPath}: visible body text contains emoji`);

  const bannedWords = [
    'seamless', 'effortless', 'powerful', 'cutting-edge', 'revolutionize',
    'unlock', 'elevate', 'empower', 'leverage', 'journey', 'next-generation',
    'ai-powered', 'your health, simplified', 'take control of your health',
  ];
  const lowerVisible = visibleText.toLowerCase();
  for (const word of bannedWords) {
    assert.ok(!lowerVisible.includes(word), `${relPath}: contains banned buzzword "${word}"`);
    checks++;
  }
  assert.ok(
    visibleText.includes('SehatSaathi is a support tool, not a doctor. In an emergency call 112.'),
    `${relPath}: missing persistent disclaimer`
  );
  checks += 4;

  // 8. Verified repo counts & empty contact fields match SITE_CONFIG
  assert.ok(siteTs.includes("SUPPORT_PHONE: ''") && siteTs.includes("SUPPORT_EMAIL: ''"), 'src/config/site.ts contact fields must stay blank');
  assert.ok(mainScript.includes("SUPPORT_PHONE: ''") && mainScript.includes("SUPPORT_EMAIL: ''"), `${relPath}: SITE_CONFIG contact fields must stay blank`);
  assert.ok(visibleText.includes('77') && visibleText.includes('30') && visibleText.includes('60'), `${relPath}: missing verified repo metrics 77 / 30 / 60`);
  assert.ok(html.includes('will-change: transform;') && html.includes('.hero-section.is-offscreen .floating-hero-card'), `${relPath}: missing GPU compositing will-change / off-screen pause for floating hero cards`);
  assert.ok(
    html.includes('id="toastRegion"') &&
    html.includes('id="ssToast"') &&
    html.includes('id="ssToastClose"') &&
    mainScript.includes('function showToast(') &&
    mainScript.includes('Log Reading saved:') &&
    mainScript.includes('Settings updated:'),
    `${relPath}: missing lightweight toast component or required action triggers`
  );
  checks += 5;

  return { checks };
}

// Check manifests for zero em dashes
for (const mPath of ['manifest.json', 'docs/manifest.json']) {
  const raw = fs.readFileSync(path.join(rootDir, mPath), 'utf8');
  assert.ok(!raw.includes('—'), `${mPath}: contains em dash`);
  JSON.parse(raw);
}

const r1 = checkHtmlFile('index.html', rootDir);
const r2 = checkHtmlFile('docs/index.html', path.join(rootDir, 'docs'));

// Verify index.html and docs/index.html are in exact sync (except ./docs/ -> ./)
const rootHtml = fs.readFileSync(path.join(rootDir, 'index.html'), 'utf8');
const docsHtml = fs.readFileSync(path.join(rootDir, 'docs', 'index.html'), 'utf8');
assert.equal(rootHtml.replace(/href="\.\/docs\//g, 'href="./'), docsHtml, 'docs/index.html is out of sync with index.html');

console.log(`[test-home] PASS: Verified index.html (${r1.checks} assertions), docs/index.html (${r2.checks} assertions), manifests, and exact sync.`);
