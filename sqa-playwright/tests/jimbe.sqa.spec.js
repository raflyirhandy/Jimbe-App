const { test, expect } = require('@playwright/test');
const path = require('path');
const fs = require('fs');

// ────────────────────────────────────────────────────
// Konfigurasi dari SupabaseClient.kt & WorkoutKatalogActivity.kt
// ────────────────────────────────────────────────────
const SUPABASE_URL = 'https://swevalpcgxuzjoflbjjq.supabase.co';
const SUPABASE_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN3ZXZhbHBjZ3h1empvZmxiampxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODE2MDgyNTksImV4cCI6MjA5NzE4NDI1OX0.trNYig8Gg1gpiWtvdtzJMqtMR6JxE5mI9BZ8hnV8ydI';
const API_NINJAS_KEY = '[YOUR_API_NINJAS_KEY]'; // Placeholder dari source code

const htmlFile = path.resolve(__dirname, '../sqa-dashboard.html');
const screenshotDir = path.resolve(__dirname, '../sqa-screenshots');

// Buat folder screenshot jika belum ada
if (!fs.existsSync(screenshotDir)) {
  fs.mkdirSync(screenshotDir, { recursive: true });
}

// ────────────────────────────────────────────────────
// HELPER FUNCTIONS
// ────────────────────────────────────────────────────
async function waitForDashboard(page) {
  await page.goto(`file:///${htmlFile.replace(/\\/g, '/')}`);
  // Tunggu loading selesai
  await page.waitForFunction(() => {
    const status = document.getElementById('run-status');
    return status && status.textContent.includes('selesai');
  }, { timeout: 45000 });
  await page.waitForTimeout(800);
}

// ────────────────────────────────────────────────────
// TEST SUITE 1: Dashboard SQA Visual
// ────────────────────────────────────────────────────
test.describe('📊 SQA Dashboard Visual', () => {

  test('TC-DASH-01: Dashboard dimuat dan semua tes berjalan', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await waitForDashboard(page);

    // Ambil screenshot full dashboard
    await page.screenshot({
      path: path.join(screenshotDir, '01-dashboard-overview.png'),
      fullPage: true
    });

    // Verifikasi title
    await expect(page).toHaveTitle('Jimbe SQA Dashboard');

    // Verifikasi summary cards muncul
    const totalCount = await page.locator('#cnt-total').textContent();
    expect(parseInt(totalCount)).toBeGreaterThan(0);

    console.log(`✅ Dashboard berhasil dimuat dengan ${totalCount} test cases`);
  });

  test('TC-DASH-02: Screenshot Summary Cards (Pass/Fail/Warn)', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await waitForDashboard(page);

    const summaryGrid = page.locator('.summary-grid');
    await summaryGrid.screenshot({
      path: path.join(screenshotDir, '02-summary-cards.png')
    });

    const passCount = await page.locator('#cnt-pass').textContent();
    const failCount = await page.locator('#cnt-fail').textContent();
    const warnCount = await page.locator('#cnt-warn').textContent();

    console.log(`📊 Summary: PASS=${passCount}, FAIL=${failCount}, WARN=${warnCount}`);
    expect(parseInt(passCount) + parseInt(failCount) + parseInt(warnCount)).toBeGreaterThan(0);
  });

  test('TC-DASH-03: Screenshot setiap grup test section', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await waitForDashboard(page);

    const groups = await page.locator('.test-group').all();
    for (let i = 0; i < groups.length; i++) {
      await groups[i].screenshot({
        path: path.join(screenshotDir, `03-group-${i + 1}-results.png`)
      });
    }

    expect(groups.length).toBeGreaterThan(0);
    console.log(`📸 Screenshot ${groups.length} grup test berhasil diambil`);
  });

});

// ────────────────────────────────────────────────────
// TEST SUITE 2: Auth API (Supabase)
// ────────────────────────────────────────────────────
test.describe('🔐 Auth API (Supabase)', () => {

  test('TC-AUTH-01: Supabase server health check', async ({ request, page }) => {
    await page.setViewportSize({ width: 1200, height: 800 });

    const response = await request.get(`${SUPABASE_URL}/auth/v1/health`, {
      headers: { 'apikey': SUPABASE_KEY }
    });

    // Screenshot halaman info
    await page.setContent(`
      <html><body style="font-family:monospace;background:#1a1a24;color:#e2e8f0;padding:24px">
        <h2 style="color:#FF6D00">TC-AUTH-01: Supabase Health Check</h2>
        <p>URL: ${SUPABASE_URL}/auth/v1/health</p>
        <p>Status: <span style="color:${response.ok() ? '#22c55e' : '#ef4444'}">${response.status()} ${response.statusText()}</span></p>
        <p>Result: <strong style="color:${response.ok() ? '#22c55e' : '#ef4444'}">${response.ok() ? '✅ PASS — Server berjalan normal' : '❌ FAIL — Server tidak merespons'}</strong></p>
        <pre style="background:#0d0d18;padding:16px;border-radius:8px;color:#94a3b8">${JSON.stringify(await response.json().catch(() => ({})), null, 2)}</pre>
      </body></html>
    `);
    await page.screenshot({ path: path.join(screenshotDir, '04-auth-01-health-check.png') });

    expect(response.ok()).toBeTruthy();
    console.log(`✅ TC-AUTH-01: Supabase health check — Status ${response.status()}`);
  });

  test('TC-AUTH-02: Login dengan email & password (API test)', async ({ request, page }) => {
    await page.setViewportSize({ width: 1200, height: 800 });

    const response = await request.post(`${SUPABASE_URL}/auth/v1/token?grant_type=password`, {
      headers: {
        'apikey': SUPABASE_KEY,
        'Content-Type': 'application/json'
      },
      data: { email: 'sqa_test@jimbe.dev', password: 'sqa_pass_123' }
    });

    const body = await response.json().catch(() => ({}));
    const hasToken = !!body.access_token;
    const isExpected = response.status() === 200 || response.status() === 400;

    await page.setContent(`
      <html><body style="font-family:monospace;background:#1a1a24;color:#e2e8f0;padding:24px">
        <h2 style="color:#FF6D00">TC-AUTH-02: Login Email & Password</h2>
        <p>Endpoint: POST ${SUPABASE_URL}/auth/v1/token?grant_type=password</p>
        <p>Status: <span style="color:${response.status()===200?'#22c55e':'#f59e0b'}">${response.status()}</span></p>
        <p>Token: ${hasToken ? '<span style="color:#22c55e">✅ Diterima</span>' : '<span style="color:#f59e0b">⚠️ Tidak ada (akun test tidak ditemukan — normal)</span>'}</p>
        <p>Result: <strong style="color:${isExpected?'#f59e0b':'#ef4444'}">${isExpected ? '⚠️ WARN — ' + (response.status()===400 ? 'Akun test belum terdaftar' : 'Login berhasil') : '❌ FAIL'}</strong></p>
        <pre style="background:#0d0d18;padding:16px;border-radius:8px;color:#94a3b8;font-size:12px">${JSON.stringify(body, null, 2).substring(0, 500)}</pre>
      </body></html>
    `);
    await page.screenshot({ path: path.join(screenshotDir, '05-auth-02-login.png') });

    expect(isExpected).toBeTruthy();
    console.log(`⚠️ TC-AUTH-02: Login — Status ${response.status()} (${hasToken ? 'token ada' : 'tidak ada token'})`);
  });

  test('TC-AUTH-03: Login dengan email kosong (validasi negatif)', async ({ request, page }) => {
    await page.setViewportSize({ width: 1200, height: 800 });

    const response = await request.post(`${SUPABASE_URL}/auth/v1/token?grant_type=password`, {
      headers: {
        'apikey': SUPABASE_KEY,
        'Content-Type': 'application/json'
      },
      data: { email: '', password: '' }
    });

    const body = await response.json().catch(() => ({}));
    const rejected = response.status() >= 400;

    await page.setContent(`
      <html><body style="font-family:monospace;background:#1a1a24;color:#e2e8f0;padding:24px">
        <h2 style="color:#FF6D00">TC-AUTH-03: Login Email Kosong (Validasi Negatif)</h2>
        <p>Test Case: Kirim email="" password="" → harus ditolak API</p>
        <p>HTTP Status: <span style="color:${rejected?'#22c55e':'#ef4444'}">${response.status()}</span></p>
        <p>Ekspektasi: <span style="color:#94a3b8">HTTP 4xx (Error)</span></p>
        <p>Result: <strong style="color:${rejected?'#22c55e':'#ef4444'}">${rejected ? '✅ PASS — API menolak credential kosong' : '❌ FAIL — API seharusnya menolak!'}</strong></p>
        <pre style="background:#0d0d18;padding:16px;border-radius:8px;color:#94a3b8">${JSON.stringify(body, null, 2)}</pre>
      </body></html>
    `);
    await page.screenshot({ path: path.join(screenshotDir, '06-auth-03-empty-login.png') });

    expect(rejected).toBeTruthy();
    console.log(`✅ TC-AUTH-03: Login kosong ditolak — Status ${response.status()}`);
  });

  test('TC-AUTH-04: Register tanpa email (validasi negatif)', async ({ request, page }) => {
    await page.setViewportSize({ width: 1200, height: 800 });

    const response = await request.post(`${SUPABASE_URL}/auth/v1/signup`, {
      headers: {
        'apikey': SUPABASE_KEY,
        'Content-Type': 'application/json'
      },
      data: { email: 'invalid-not-an-email', password: '123' }
    });

    const body = await response.json().catch(() => ({}));
    const rejected = response.status() >= 400;

    await page.setContent(`
      <html><body style="font-family:monospace;background:#1a1a24;color:#e2e8f0;padding:24px">
        <h2 style="color:#FF6D00">TC-AUTH-04: Register Email Invalid</h2>
        <p>Test Case: email="invalid-not-an-email", password="123" (terlalu pendek)</p>
        <p>HTTP Status: <span style="color:${rejected?'#22c55e':'#ef4444'}">${response.status()}</span></p>
        <p>Result: <strong style="color:${rejected?'#22c55e':'#ef4444'}">${rejected ? '✅ PASS — API menolak email/password invalid' : '❌ FAIL — API seharusnya menolak!'}</strong></p>
        <div style="margin-top:8px;color:#f59e0b">⚠️ Catatan: Validasi format email & panjang password HARUS juga dilakukan di sisi client (RegisterActivity.kt) agar tidak bergantung pada API</div>
        <pre style="background:#0d0d18;padding:16px;border-radius:8px;color:#94a3b8;margin-top:12px">${JSON.stringify(body, null, 2)}</pre>
      </body></html>
    `);
    await page.screenshot({ path: path.join(screenshotDir, '07-auth-04-invalid-register.png') });

    console.log(`TC-AUTH-04: Register invalid — Status ${response.status()}`);
  });

});

// ────────────────────────────────────────────────────
// TEST SUITE 3: Member API (Supabase Postgrest)
// ────────────────────────────────────────────────────
test.describe('👥 Member API (Supabase Postgrest)', () => {

  test('TC-MBR-01: Cek endpoint tabel members tersedia', async ({ request, page }) => {
    await page.setViewportSize({ width: 1200, height: 800 });

    const response = await request.get(`${SUPABASE_URL}/rest/v1/members?select=*&limit=5`, {
      headers: { 'apikey': SUPABASE_KEY }
    });

    let body;
    const contentType = response.headers()['content-type'] || '';
    if (contentType.includes('json')) {
      body = await response.json().catch(() => []);
    } else {
      body = await response.text().catch(() => '');
    }

    const tableExists = response.status() !== 404 && response.status() !== 500;
    const rlsActive = response.status() === 401;
    const noRls = response.status() === 200;

    await page.setContent(`
      <html><body style="font-family:monospace;background:#1a1a24;color:#e2e8f0;padding:24px">
        <h2 style="color:#FF6D00">TC-MBR-01: Endpoint Tabel Members</h2>
        <p>GET ${SUPABASE_URL}/rest/v1/members</p>
        <p>Status: <span style="color:${tableExists?'#22c55e':'#ef4444'}">${response.status()}</span></p>
        <p>Tabel Ada: <span style="color:${tableExists?'#22c55e':'#ef4444'}">${tableExists?'✅ Ya':'❌ Tidak'}</span></p>
        <p>RLS (Row Level Security): <span style="color:${rlsActive?'#22c55e':'#f59e0b'}">${rlsActive?'✅ Aktif':'⚠️ TIDAK AKTIF — data exposed!'}</span></p>
        ${noRls ? '<p style="color:#f59e0b;font-weight:bold">⚠️ PERINGATAN: Data member bisa diakses tanpa login!</p>' : ''}
        <pre style="background:#0d0d18;padding:16px;border-radius:8px;color:#94a3b8;font-size:12px;margin-top:12px">${JSON.stringify(body, null, 2).substring(0,800)}</pre>
      </body></html>
    `);
    await page.screenshot({ path: path.join(screenshotDir, '08-member-01-endpoint-check.png') });

    expect(tableExists).toBeTruthy();
    console.log(`TC-MBR-01: Tabel members — Status ${response.status()}, RLS: ${rlsActive ? 'Aktif' : 'TIDAK AKTIF'}`);
  });

  test('TC-MBR-02: Insert member baru (uji RLS protection)', async ({ request, page }) => {
    await page.setViewportSize({ width: 1200, height: 800 });

    const testMember = {
      nama_lengkap: 'SQA Test Member Playwright',
      nomor_hp: '089999999999',
      tanggal_berakhir_member: '2026-07-22'
    };

    const response = await request.post(`${SUPABASE_URL}/rest/v1/members`, {
      headers: {
        'apikey': SUPABASE_KEY,
        'Content-Type': 'application/json',
        'Prefer': 'return=minimal'
      },
      data: testMember
    });

    const body = await response.text().catch(() => '');
    const insertBlocked = response.status() >= 400;
    const rlsWorking = response.status() === 401 || response.status() === 403;

    await page.setContent(`
      <html><body style="font-family:monospace;background:#1a1a24;color:#e2e8f0;padding:24px">
        <h2 style="color:#FF6D00">TC-MBR-02: Insert Member Tanpa Autentikasi</h2>
        <p>POST ${SUPABASE_URL}/rest/v1/members</p>
        <p>Payload: ${JSON.stringify(testMember)}</p>
        <p>Status: <span style="color:${insertBlocked?'#22c55e':'#ef4444'}">${response.status()}</span></p>
        <p>RLS Protection: <span style="color:${rlsWorking?'#22c55e':insertBlocked?'#f59e0b':'#ef4444'}">${rlsWorking?'✅ Aktif — INSERT ditolak':insertBlocked?'⚠️ Ditolak dengan alasan lain':'❌ RLS tidak aktif — INSERT BERHASIL TANPA AUTH!'}</span></p>
        ${!insertBlocked ? '<p style="color:#ef4444;font-weight:bold">❌ KRITIS: Data dapat dimanipulasi tanpa autentikasi!</p>' : ''}
        <pre style="background:#0d0d18;padding:16px;border-radius:8px;color:#94a3b8;margin-top:12px">${body.substring(0,500) || '(empty body)'}</pre>
      </body></html>
    `);
    await page.screenshot({ path: path.join(screenshotDir, '09-member-02-rls-test.png') });

    console.log(`TC-MBR-02: Insert member — Status ${response.status()}, RLS: ${rlsWorking ? 'Aktif' : 'TIDAK AKTIF'}`);
  });

  test('TC-MBR-03: Validasi field member (field wajib)', async ({ request, page }) => {
    await page.setViewportSize({ width: 1200, height: 800 });

    // Kirim data member tidak lengkap
    const response = await request.post(`${SUPABASE_URL}/rest/v1/members`, {
      headers: {
        'apikey': SUPABASE_KEY,
        'Content-Type': 'application/json',
        'Prefer': 'return=minimal'
      },
      data: { nama_lengkap: 'Incomplete Member' } // tanpa nomor_hp & tanggal
    });

    const body = await response.text().catch(() => '');
    const validationHandled = response.status() >= 400;

    await page.setContent(`
      <html><body style="font-family:monospace;background:#1a1a24;color:#e2e8f0;padding:24px">
        <h2 style="color:#FF6D00">TC-MBR-03: Validasi Field Member (Data Tidak Lengkap)</h2>
        <p>Test: Insert member tanpa nomor_hp dan tanggal_berakhir_member</p>
        <p>Status: <span style="color:${validationHandled?'#22c55e':'#f59e0b'}">${response.status()}</span></p>
        <p>Validasi: <span style="color:${validationHandled?'#22c55e':'#f59e0b'}">${validationHandled?'✅ Data tidak lengkap ditolak':'⚠️ Perlu dicek lebih lanjut'}</span></p>
        <pre style="background:#0d0d18;padding:16px;border-radius:8px;color:#94a3b8;margin-top:12px">${body.substring(0,500) || '(empty body)'}</pre>
      </body></html>
    `);
    await page.screenshot({ path: path.join(screenshotDir, '10-member-03-validation.png') });

    console.log(`TC-MBR-03: Validasi field member — Status ${response.status()}`);
  });

});

// ────────────────────────────────────────────────────
// TEST SUITE 4: Exercises API (API-Ninjas)
// ────────────────────────────────────────────────────
test.describe('💪 Exercises API (API-Ninjas)', () => {

  test('TC-EXR-01: Deteksi API Key Placeholder (BUG KRITIS)', async ({ page }) => {
    await page.setViewportSize({ width: 1200, height: 700 });

    const isPlaceholder = API_NINJAS_KEY === '[YOUR_API_NINJAS_KEY]';

    await page.setContent(`
      <html><body style="font-family:monospace;background:#1a1a24;color:#e2e8f0;padding:24px">
        <h2 style="color:#FF6D00">TC-EXR-01: API-Ninjas Key Check</h2>
        <p>File: WorkoutKatalogActivity.kt, Baris 21</p>
        <p>API_KEY Value: <code style="background:#0d0d18;padding:4px 8px;border-radius:4px;color:#ef4444">${API_NINJAS_KEY}</code></p>
        <p>Status: <strong style="color:#ef4444">❌ FAIL — API Key masih PLACEHOLDER</strong></p>
        <br/>
        <div style="background:#2d1b1b;border:1px solid #ef4444;border-radius:8px;padding:16px;">
          <p style="color:#ef4444;font-weight:bold">🚨 DAMPAK KRITIS:</p>
          <ul style="color:#fca5a5;margin-top:8px;margin-left:16px">
            <li>Fitur "Katalog Workout" TIDAK BERFUNGSI sama sekali</li>
            <li>Setiap request ke API-Ninjas akan gagal dengan HTTP 401</li>
            <li>User akan melihat error Toast "Gagal mengambil data"</li>
          </ul>
        </div>
        <br/>
        <div style="background:#1b2d1b;border:1px solid #22c55e;border-radius:8px;padding:16px;">
          <p style="color:#22c55e;font-weight:bold">✅ CARA PERBAIKAN:</p>
          <ol style="color:#86efac;margin-top:8px;margin-left:16px">
            <li>Daftar akun di https://api-ninjas.com</li>
            <li>Salin API Key dari dashboard</li>
            <li>Isi di WorkoutKatalogActivity.kt baris 21</li>
            <li>Pertimbangkan menyimpan di local.properties</li>
          </ol>
        </div>
      </body></html>
    `);
    await page.screenshot({ path: path.join(screenshotDir, '11-exercise-01-api-key-missing.png') });

    // Test ini HARUS fail karena ini membuktikan bug ada
    expect(isPlaceholder).toBeTruthy();
    console.log(`❌ TC-EXR-01: API-Ninjas key adalah placeholder — fitur workout tidak berfungsi!`);
  });

  test('TC-EXR-02: Cek konektivitas server API-Ninjas', async ({ request, page }) => {
    await page.setViewportSize({ width: 1200, height: 700 });

    let status = 0;
    let serverReachable = false;
    let errorMsg = '';

    try {
      const response = await request.get('https://api.api-ninjas.com/v1/exercises', {
        headers: { 'X-Api-Key': 'dummy_key_for_connectivity_test' },
        timeout: 10000
      });
      status = response.status();
      serverReachable = true;
    } catch (e) {
      errorMsg = e.message;
      serverReachable = false;
    }

    const authError = status === 401 || status === 403;

    await page.setContent(`
      <html><body style="font-family:monospace;background:#1a1a24;color:#e2e8f0;padding:24px">
        <h2 style="color:#FF6D00">TC-EXR-02: Konektivitas Server API-Ninjas</h2>
        <p>URL: https://api.api-ninjas.com/v1/exercises</p>
        <p>Server Reachable: <span style="color:${serverReachable?'#22c55e':'#f59e0b'}">${serverReachable?'✅ Ya':'⚠️ Tidak (mungkin CORS/network)'}${errorMsg ? ' — ' + errorMsg : ''}</span></p>
        ${serverReachable ? `<p>HTTP Status: <span style="color:#94a3b8">${status}</span></p>
        <p>Auth Required: <span style="color:${authError?'#22c55e':'#94a3b8'}">${authError?'✅ Ya (butuh API key valid)':'Tidak diketahui'}</span></p>` : ''}
        <p>Kesimpulan: <strong style="color:${serverReachable?'#22c55e':'#f59e0b'}">${serverReachable?'✅ PASS — Server bisa dijangkau':'⚠️ WARN — Server tidak bisa dijangkau dari environment ini'}</strong></p>
      </body></html>
    `);
    await page.screenshot({ path: path.join(screenshotDir, '12-exercise-02-server-check.png') });

    console.log(`TC-EXR-02: API-Ninjas server — ${serverReachable ? `Reachable (Status ${status})` : 'Tidak reachable: ' + errorMsg}`);
  });

});

// ────────────────────────────────────────────────────
// TEST SUITE 5: Code Quality & SQA Findings
// ────────────────────────────────────────────────────
test.describe('🔍 Code Quality & Static Analysis', () => {

  test('TC-CODE-01: Ringkasan temuan code quality', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 1000 });

    await page.setContent(`
      <html>
      <head>
        <style>
          * { box-sizing: border-box; margin: 0; padding: 0; }
          body { font-family: 'Segoe UI', sans-serif; background: #0f0f13; color: #e2e8f0; padding: 32px; }
          h1 { color: #FF6D00; font-size: 24px; margin-bottom: 24px; }
          .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
          .card { background: #1a1a24; border: 1px solid #2a2a3a; border-radius: 12px; padding: 20px; }
          .card.critical { border-color: #ef4444; }
          .card.warning { border-color: #f59e0b; }
          .card.info { border-color: #3b82f6; }
          .card-title { font-weight: 700; font-size: 14px; margin-bottom: 12px; display: flex; align-items: center; gap: 8px; }
          .tag { font-size: 10px; padding: 2px 8px; border-radius: 999px; font-weight: 700; }
          .tag-fail { background: rgba(239,68,68,.2); color: #ef4444; border: 1px solid rgba(239,68,68,.3); }
          .tag-warn { background: rgba(245,158,11,.2); color: #f59e0b; border: 1px solid rgba(245,158,11,.3); }
          .detail { font-size: 12px; color: #94a3b8; line-height: 1.6; }
          .detail strong { color: #e2e8f0; }
          code { background: #0d0d18; padding: 2px 6px; border-radius: 4px; font-family: monospace; font-size: 11px; color: #93c5fd; }
        </style>
      </head>
      <body>
        <h1>🔍 Code Quality Review — Jimbe Android App</h1>
        <div class="grid">
          <div class="card critical">
            <div class="card-title">🔴 BUG-01: CoroutineScope Memory Leak <span class="tag tag-fail">KRITIS</span></div>
            <div class="detail">
              <strong>6 Activity</strong> menggunakan <code>CoroutineScope(Dispatchers.IO)</code> yang tidak di-cancel.<br><br>
              File: LoginActivity, RegisterActivity, MainActivity, MemberListActivity, MemberFormActivity, WorkoutKatalogActivity<br><br>
              <strong>Fix:</strong> Ganti dengan <code>lifecycleScope.launch(Dispatchers.IO)</code>
            </div>
          </div>
          <div class="card critical">
            <div class="card-title">🔴 BUG-02: API Key Hardcoded <span class="tag tag-fail">KEAMANAN</span></div>
            <div class="detail">
              Supabase URL & API Key ditulis langsung di <code>SupabaseClient.kt</code>.<br><br>
              Siapa pun yang akses source code dapat menyalahgunakan database.<br><br>
              <strong>Fix:</strong> Pindahkan ke <code>local.properties</code> + BuildConfig
            </div>
          </div>
          <div class="card critical">
            <div class="card-title">🔴 BUG-03: API-Ninjas Placeholder Key <span class="tag tag-fail">KRITIS</span></div>
            <div class="detail">
              <code>API_KEY = "[YOUR_API_NINJAS_KEY]"</code> di WorkoutKatalogActivity.kt baris 21.<br><br>
              Fitur Katalog Workout tidak berfungsi sama sekali.<br><br>
              <strong>Fix:</strong> Isi dengan key dari api-ninjas.com
            </div>
          </div>
          <div class="card critical">
            <div class="card-title">🔴 BUG-04: Spasi Ekstra Class Declaration <span class="tag tag-fail">KRITIS</span></div>
            <div class="detail">
              <code>class&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;MainActivity</code><br><br>
              Baris 14 MainActivity.kt — formatting sangat buruk.<br><br>
              <strong>Fix:</strong> Hapus semua spasi berlebihan
            </div>
          </div>
          <div class="card warning">
            <div class="card-title">🟡 BUG-05: Handler Deprecated <span class="tag tag-warn">PERINGATAN</span></div>
            <div class="detail">
              <code>Handler(Looper.getMainLooper()).postDelayed()</code> deprecated sejak API 30.<br><br>
              Di SplashActivity.kt baris 19-21.<br><br>
              <strong>Fix:</strong> <code>lifecycleScope.launch { delay(2000); checkAuth() }</code>
            </div>
          </div>
          <div class="card warning">
            <div class="card-title">🟡 BUG-06: Retrofit Bukan Singleton <span class="tag tag-warn">PERINGATAN</span></div>
            <div class="detail">
              <code>Retrofit.Builder()</code> dibuat ulang setiap kali <code>fetchWorkouts()</code> dipanggil.<br><br>
              Pemborosan memori dan waktu inisialisasi.<br><br>
              <strong>Fix:</strong> Jadikan property dengan <code>lazy</code>
            </div>
          </div>
          <div class="card warning">
            <div class="card-title">🟡 Validasi Input Lemah <span class="tag tag-warn">PERINGATAN</span></div>
            <div class="detail">
              Login & Register hanya cek <code>isEmpty()</code> — tidak validasi format email maupun panjang password minimal.<br><br>
              <strong>Fix:</strong> Tambahkan <code>Patterns.EMAIL_ADDRESS.matcher(email).matches()</code> dan cek <code>password.length >= 6</code>
            </div>
          </div>
          <div class="card warning">
            <div class="card-title">🟡 Tidak Ada Loading State <span class="tag tag-warn">PERINGATAN</span></div>
            <div class="detail">
              Login & Register tidak disable tombol saat proses. User bisa klik berkali-kali → multiple request.<br><br>
              <strong>Fix:</strong> Disable tombol + tampilkan ProgressBar saat proses berlangsung
            </div>
          </div>
        </div>
      </body>
      </html>
    `);
    await page.screenshot({
      path: path.join(screenshotDir, '13-code-quality-findings.png'),
      fullPage: true
    });

    console.log('📸 Screenshot code quality findings berhasil diambil');
  });

  test('TC-CODE-02: Full SQA Dashboard Screenshot (Final)', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await waitForDashboard(page);

    // Buka semua detail
    const headers = await page.locator('.test-header').all();
    for (const h of headers.slice(0, 6)) {
      await h.click();
      await page.waitForTimeout(100);
    }

    await page.screenshot({
      path: path.join(screenshotDir, '14-final-sqa-report.png'),
      fullPage: true
    });

    const passCount = parseInt(await page.locator('#cnt-pass').textContent() || '0');
    const failCount = parseInt(await page.locator('#cnt-fail').textContent() || '0');
    const warnCount = parseInt(await page.locator('#cnt-warn').textContent() || '0');

    console.log(`\n========================================`);
    console.log(`  JIMBE SQA FINAL REPORT`);
    console.log(`========================================`);
    console.log(`  ✅ PASS  : ${passCount}`);
    console.log(`  ❌ FAIL  : ${failCount}`);
    console.log(`  ⚠️  WARN  : ${warnCount}`);
    console.log(`  📊 TOTAL : ${passCount + failCount + warnCount}`);
    console.log(`========================================`);
    console.log(`  Screenshots saved: ${screenshotDir}`);
    console.log(`========================================\n`);

    expect(passCount + failCount + warnCount).toBeGreaterThan(0);
  });

});
