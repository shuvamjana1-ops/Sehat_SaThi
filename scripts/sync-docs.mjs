#!/usr/bin/env node
/**
 * Syncs root /index.html into /docs/index.html for static Vercel/GitHub Pages output,
 * rewriting ./docs/ relative links to ./ inside /docs/.
 */
import fs from 'node:fs';
import path from 'node:path';

const rootDir = process.cwd();
const srcPath = path.join(rootDir, 'index.html');
const dstPath = path.join(rootDir, 'docs', 'index.html');

const html = fs.readFileSync(srcPath, 'utf8');
const synced = html.replace(/href="\.\/docs\//g, 'href="./');

fs.writeFileSync(dstPath, synced, 'utf8');
console.log('[sync-docs] Synced index.html -> docs/index.html');
