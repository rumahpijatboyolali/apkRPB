# APK Rumah Pijat Boyolali

Proyek Android native tipis yang membuka admin web app Google Apps Script di WebView. Koneksi internet diperlukan; data tetap diproses oleh Apps Script dan spreadsheet seperti versi web.

## Membuat APK

1. Buka folder `android/` di Android Studio.
2. Pastikan Android SDK 35 terpasang, lalu pilih **Build > Build APK(s)**.
3. APK debug biasanya tersedia di `app/build/outputs/apk/debug/app-debug.apk`.

URL deployment di `app/src/main/java/id/rumahpijatboyolali/app/MainActivity.java` diambil dari form dan tautan admin dalam `github-pages-index.html`. Jika deployment Apps Script berubah, perbarui `APP_URL`.

APK ini belum dapat dibangun di lingkungan proyek saat ini karena Android SDK/Gradle tidak tersedia. Gunakan Android Studio untuk membangunnya.
