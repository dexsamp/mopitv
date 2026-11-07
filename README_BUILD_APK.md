# MoppiTV - Universal Android APK Project (Android 5.0 Lollipop s/d Android 16)

Project ini telah dikonfigurasi ulang dan diperbaiki agar **kompatibel dengan semua versi Android aktif (mulai dari Android 5.0 Lollipop / API 21 hingga Android 15/16 / API 35+)** serta mendukung seluruh arsitektur prosesor (`armeabi-v7a` 32-bit, `arm64-v8a` 64-bit, `x86`, dan `x86_64`).

## Perbaikan yang Telah Diterapkan
1. **Dukungan Semua Versi Android (`minSdk = 21`)**: Menurunkan batas minimum SDK dari API 24 ke API 21 (Android 5.0+), mengaktifkan `multiDexEnabled = true`, serta menambahkan `coreLibraryDesugaring` (`desugar_jdk_libs`).
2. **Kompatibilitas API Lama di Kode Kotlin**:
   - Menambahkan fallback `ConnectivityManager.activeNetworkInfo` untuk Android 5.0–5.1 pada `AnimeScraperApi.kt`.
   - Menambahkan overload `WebViewClient.shouldOverrideUrlLoading(WebView?, String?)` untuk Android 5.0–6.0 pada `RealAnimeVideoPlayer.kt`.
   - Menambahkan pengaman `try-catch` pada inisialisasi `Firebase.auth` di `WatchPartyFirestoreRepository.kt` agar aplikasi tidak *force close* saat berjalan tanpa `google-services.json`.
3. **Perbaikan Versi Gradle & Plugin Publik**: Mengganti versi internal AI Studio (`agp 9.1.1`, `gradle 9.3.1`, `firebase-bom 34.17.0`) ke versi rilis stabil publik Google Maven (`AGP 8.7.3`, `Gradle 8.11.1`, `Kotlin 2.0.21`, `Firebase BOM 33.7.0`).
4. **Keystore Penandatanganan Otomatis (Signed APK)**: Menyertakan `my-upload-key.jks` (Release) dan `debug.keystore` (Debug) dengan *signing V1 + V2 + V3* agar APK dapat langsung diinstal di semua perangkat Android.

## Cara Build menjadi File `.apk` Tanpa Install Android Studio (Via GitHub Actions Gratis)
1. Buat repository baru di GitHub, lalu unggah (push/upload) seluruh isi folder project ini (termasuk folder `.github/workflows/build-apk.yml`).
2. Buka tab **Actions** di repository GitHub kamu, lalu klik workflow **Build MoppiTV Universal Android APK** -> **Run workflow**.
3. Tunggu sekitar 2–3 menit hingga proses selesai, lalu unduh **`MoppiTV-Universal-Release-APK`** langsung dari bagian **Artifacts** di halaman tersebut.

## Cara Build di Komputer (Android Studio / Command Line)
- Buka folder ini di **Android Studio**, tunggu sinkronisasi Gradle selesai, lalu pilih menu **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
- Atau jalankan lewat terminal jika Gradle sudah terinstal:
  ```bash
  gradle assembleRelease
  ```
  File APK siap pakai akan tersedia di `app/build/outputs/apk/release/app-release.apk`.
