# Stok Pelampung Pancing (Aplikasi Android)

Aplikasi untuk mencatat stok pelampung pancing di gudang: **barang masuk**, **barang keluar**,
dan **jumlah yang tersisa di gudang** (dihitung otomatis). Data tersimpan di HP, tidak perlu internet.

## Fitur

- **Daftar barang**: nama barang, total stok masuk, total barang keluar, dan jumlah di gudang.
- **Total di gudang** dan jumlah barang yang **stoknya menipis** (batas bisa diatur per barang).
- **Barang Masuk / Barang Keluar**: buka barang, tekan tombolnya, lalu isi jumlah dan catatan
  (misalnya "kiriman supplier" atau "dijual ke Toko Jaya").
  Barang keluar tidak bisa melebihi stok di gudang.
- **Riwayat**: semua catatan masuk/keluar lengkap dengan tanggal, bisa disaring per jenis.
- **Cari barang** berdasarkan nama atau keterangan (warna, ukuran, merek).
- **Ubah/hapus barang** dan **hapus catatan** yang salah input (stok dihitung ulang).
- **Bagikan laporan stok** sebagai teks ke WhatsApp, Telegram, dan lain-lain (ikon bagikan di kanan atas).

Syarat: Android 8.0 ke atas.

---

## Cara 1: Dapatkan APK lewat GitHub (tanpa memasang apa pun di komputer)

1. Buat akun di https://github.com (gratis), lalu buat repository baru, misalnya `stok-pelampung`.
2. Di halaman repository, klik **Add file → Upload files**, lalu tarik **seluruh isi** folder ini
   (termasuk folder `.github`, `app`, `gradle`, dan file `gradlew`). Klik **Commit changes**.
   > Folder `.github` kadang tersembunyi. Jika tidak ikut terunggah, buat file
   > `.github/workflows/build-apk.yml` lewat **Add file → Create new file** lalu salin isinya.
3. Buka tab **Actions**. Proses "Build APK" akan berjalan sekitar 5–10 menit.
4. Setelah selesai (tanda centang hijau), klik proses tersebut. Di bagian **Artifacts**,
   unduh **StokPelampung-apk**, lalu ekstrak file zip-nya untuk mendapatkan `app-debug.apk`.
5. Kirim `app-debug.apk` ke HP dan buka. Jika diminta, izinkan "Instal dari sumber tidak dikenal".

## Cara 2: Build dengan Android Studio

1. Pasang Android Studio dari https://developer.android.com/studio.
2. Pilih **File → Open**, lalu pilih folder `StokPelampung` ini. Tunggu proses "Gradle sync" selesai
   (perlu internet saat pertama kali).
3. Sambungkan HP (aktifkan *USB debugging*) atau pakai emulator, lalu tekan tombol **Run ▶**.
4. Untuk file APK: **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
   Hasilnya ada di `app/build/outputs/apk/debug/app-debug.apk`.

---

## Struktur kode

| File | Isi |
|------|-----|
| `data/Models.kt` | Data Barang & Transaksi; sisa = total masuk − total keluar |
| `data/StokRepository.kt` | Database SQLite di HP |
| `ui/StokViewModel.kt` | Logika aplikasi & teks laporan |
| `MainActivity.kt` | Layar utama dan navigasi tab |
| `ui/BerandaScreen.kt` | Daftar stok dan ringkasan gudang |
| `ui/DetailScreen.kt` | Detail barang, tombol masuk/keluar, riwayat per barang |
| `ui/RiwayatScreen.kt` | Riwayat semua transaksi |
| `ui/Dialogs.kt` | Form tambah/ubah barang dan form masuk/keluar |

Dibuat dengan Kotlin + Jetpack Compose (Material 3).
