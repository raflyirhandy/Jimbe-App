# Jimbe

Jimbe adalah proyek aplikasi Android.

## Persyaratan Sistem

- Android Studio (disarankan versi terbaru)
- Android SDK
- Gradle

## Cara Menjalankan

1. Clone repositori ini.
2. Buka proyek di **Android Studio**.
3. Biarkan Gradle mensinkronisasi dependensi proyek.
4. Jalankan aplikasi pada Emulator Android atau perangkat fisik menggunakan konfigurasi `app`.

## Struktur Proyek

- `app/` - Berisi kode dan sumber daya utama aplikasi Android.
- `sqa-playwright/` - Berisi pengujian Software Quality Assurance (e2e) menggunakan Playwright.
- `gradle/` - Berisi file Gradle wrapper.

## Pengujian

Untuk menjalankan pengujian unit Android:
```bash
./gradlew test
```

Untuk menjalankan pengujian instrumen Android:
```bash
./gradlew connectedAndroidTest
```

Untuk pengujian E2E Playwright, silakan merujuk ke instruksi di dalam direktori `sqa-playwright`.
