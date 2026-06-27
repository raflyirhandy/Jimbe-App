const { defineConfig } = require('@playwright/test');
const path = require('path');
const os = require('os');

// Path ke Chromium yang sudah terinstall oleh Playwright
const chromiumExe = path.join(
  os.homedir(),
  'AppData', 'Local', 'ms-playwright',
  'chromium-1228', 'chrome-win64', 'chrome.exe'
);

module.exports = defineConfig({
  testDir: './tests',
  timeout: 60000,
  fullyParallel: false,
  reporter: [
    ['list'],
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['json', { outputFile: 'test-results/results.json' }]
  ],
  use: {
    screenshot: 'on',
    video: 'off',
    trace: 'on-first-retry',
    headless: true,
    launchOptions: {
      executablePath: chromiumExe,
    },
  },
  projects: [
    {
      name: 'chromium',
      use: {
        browserName: 'chromium',
        headless: true,
        launchOptions: {
          executablePath: chromiumExe,
        },
      },
    },
  ],
  outputDir: 'test-results/',
});

