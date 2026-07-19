# SPBU Receipt Printer 🔴⛽

Aplikasi Android Native untuk membuat dan mencetak struk/nota SPBU menggunakan printer Bluetooth Thermal 58mm dengan standar ESC/POS.

---

## 📱 Fitur Lengkap

| Fitur | Status |
|---|---|
| Form Transaksi SPBU | ✅ |
| Perhitungan otomatis (Harga × Volume = Total) | ✅ |
| Preview Struk sebelum cetak | ✅ |
| Cetak ESC/POS ke printer Bluetooth 58mm / 80mm | ✅ |
| Scan & Connect printer Bluetooth | ✅ |
| Auto-reconnect printer terakhir | ✅ |
| Test Print | ✅ |
| Riwayat Transaksi (tanpa batas) | ✅ |
| Pencarian & Filter transaksi | ✅ |
| Edit & Hapus transaksi | ✅ |
| Cetak Ulang dari Riwayat | ✅ |
| Export PDF (per transaksi) | ✅ |
| Export Excel (semua transaksi) | ✅ |
| Share PDF | ✅ |
| Pengaturan Harga BBM (CRUD) | ✅ |
| Dark Mode | ✅ |
| Backup & Restore Database | ✅ |
| Upload Logo SPBU | ✅ |
| QR Code di struk | ✅ |
| Barcode di struk | ✅ |
| Jumlah Copy 1–10 | ✅ |
| Pilihan lebar kertas 58mm / 80mm | ✅ |
| Splash Screen | ✅ |
| Material Design 3 | ✅ |

---

## 🛠️ Teknologi

- **Kotlin** + **Jetpack Compose**
- **Material Design 3** (tema merah Pertamina)
- **MVVM** + **Repository Pattern**
- **Room Database** (SQLite)
- **Hilt** (Dependency Injection)
- **Navigation Compose**
- **Coroutines** + **StateFlow**

### Library Pihak Ketiga

| Library | Fungsi |
|---|---|
| [DantSu ESC/POS Printer](https://github.com/DantSu/ESCPOS-ThermalPrinter-Android) | Cetak ESC/POS Bluetooth |
| [iText 7](https://itextpdf.com) | Export PDF |
| [Apache POI](https://poi.apache.org) | Export Excel |
| [ZXing](https://github.com/zxing/zxing) | QR Code & Barcode |
| [Coil](https://coil-kt.github.io/coil/) | Load gambar logo |
| [Accompanist Permissions](https://google.github.io/accompanist/permissions/) | Runtime permissions |

---

## 🚀 Cara Instalasi

### Prasyarat
- **Android Studio Hedgehog** (2023.1.1) atau lebih baru
- **JDK 17**
- **Android SDK** API 26–35
- Perangkat Android **minimal Android 8.0 (API 26)**

### Langkah

1. **Ekstrak ZIP** project ke folder pilihan Anda

2. **Buka Android Studio** → `File` → `Open` → pilih folder `SPBUReceiptPrinter`

3. **Tunggu Gradle Sync** selesai (butuh koneksi internet untuk download dependencies)

4. **Build & Run** ke perangkat Android atau emulator

> ⚠️ Bluetooth tidak berfungsi di emulator. Gunakan perangkat fisik untuk test cetak.

---

## 📲 Cara Menggunakan

### 1. Pertama Kali Buka Aplikasi
- Aplikasi langsung masuk ke **Dashboard**
- Data harga BBM default sudah terisi otomatis
- Pengaturan SPBU default perlu diisi di menu **Pengaturan**

### 2. Konfigurasi Pengaturan SPBU
- Tap **Pengaturan** dari dashboard
- Isi **Nama SPBU**, **Nomor SPBU**, **Alamat**
- Atur **Footer Struk** (contoh: "Terima Kasih\nSelamat Jalan")
- Pilih **Lebar Kertas** (58mm atau 80mm)
- Tap **Simpan Pengaturan**

### 3. Hubungkan Printer Bluetooth
- Pastikan printer thermal **sudah dinyalakan**
- Di HP, buka **Pengaturan → Bluetooth → Pair** printer
- Kembali ke aplikasi → tap **Printer**
- Aplikasi akan menampilkan daftar perangkat paired
- Tap **Hubungkan** pada printer yang diinginkan
- Tap **Test Print** untuk verifikasi

### 4. Buat Transaksi
- Tap **Buat Transaksi** dari dashboard
- Isi semua field yang bertanda `*`
- Pilih **Jenis BBM** → harga otomatis terisi dari database
- Isi **Volume** → **Total** otomatis terhitung
- *Atau* isi **Total** → **Volume** otomatis terhitung
- Tap **Preview & Cetak**

### 5. Preview dan Cetak
- Lihat preview struk di layar
- Tap **Cetak Struk** untuk mencetak
- Tap **Edit** jika perlu koreksi
- Tap **Selesai** untuk kembali ke dashboard

### 6. Riwayat
- Tap **Riwayat** dari dashboard
- Cari transaksi dengan search bar
- Filter berdasarkan rentang tanggal
- Opsi: **Preview**, **Cetak Ulang**, **Edit**, **Hapus**
- Tap ikon Excel (kanan atas) untuk export semua data

---

## 🖨️ Printer yang Didukung

Semua printer Bluetooth Thermal 58mm/80mm yang mendukung standar ESC/POS, termasuk:

- **CX-588** / CX-58BT
- **Panda** series
- **XPrinter** XP-P320B / XP-58IIH
- **MPT-II** / MPOS
- **HOIN** / HOP-E58
- **Goojprt** PT-210 / PT-260
- Semua printer ESC/POS Bluetooth lainnya

---

## 🗄️ Struktur Database

### Tabel `transaksi`
| Field | Tipe | Keterangan |
|---|---|---|
| id | INTEGER PK | Auto-generate |
| nomorTransaksi | TEXT | Nomor custom dari user |
| tanggal | TEXT | Format: dd/MM/yyyy |
| jam | TEXT | Format: HH:mm:ss |
| shift | TEXT | Nomor shift |
| operator | TEXT | Nama operator |
| pembayaran | TEXT | CASH/QRIS/Debit/Kredit |
| nomorPlat | TEXT | Plat kendaraan |
| produk | TEXT | Nama produk BBM |
| hargaLiter | REAL | Harga/liter saat transaksi |
| volume | REAL | Volume dalam liter |
| total | REAL | Total harga |
| createdAt | INTEGER | Unix timestamp |

### Tabel `produk_bbm`
| Field | Tipe | Keterangan |
|---|---|---|
| id | INTEGER PK | Auto-generate |
| namaProduk | TEXT | Nama BBM |
| harga | REAL | Harga default/liter |
| urutan | INTEGER | Urutan di dropdown |
| aktif | INTEGER | 1=aktif, 0=nonaktif |

### Tabel `setting`
| Field | Tipe | Keterangan |
|---|---|---|
| id | INTEGER PK | Selalu 1 |
| namaSpbu | TEXT | Nama SPBU |
| nomorSpbu | TEXT | Nomor SPBU |
| alamat | TEXT | Alamat |
| footer | TEXT | Footer struk |
| logoPath | TEXT | URI logo |
| printerDefault | TEXT | MAC address printer |
| namaPrinter | TEXT | Nama printer default |
| ukuranFont | TEXT | SMALL/MEDIUM/LARGE |
| jumlahCopy | INTEGER | 1–10 |
| lebarKertas | INTEGER | 58 atau 80 |
| darkMode | INTEGER | 0/1 |
| marginKiri | INTEGER | Karakter margin |

---

## 📁 Struktur Project

```
SPBUReceiptPrinter/
├── app/
│   ├── src/main/
│   │   ├── java/com/spbu/receiptprinter/
│   │   │   ├── bluetooth/
│   │   │   │   ├── BluetoothPrinterManager.kt   # Core Bluetooth + ESC/POS
│   │   │   │   └── EscPosFormatter.kt            # Format struk ESC/POS
│   │   │   ├── data/
│   │   │   │   ├── dao/                          # Room DAO
│   │   │   │   ├── database/                     # AppDatabase + Initializer
│   │   │   │   ├── model/                        # Entity (Transaksi, ProdukBbm, Setting)
│   │   │   │   └── repository/                   # Repository pattern
│   │   │   ├── di/
│   │   │   │   └── DatabaseModule.kt             # Hilt DI module
│   │   │   ├── ui/
│   │   │   │   ├── common/                       # Komponen reusable + Theme
│   │   │   │   ├── dashboard/                    # Dashboard
│   │   │   │   ├── history/                      # Riwayat transaksi
│   │   │   │   ├── preview/                      # Preview struk
│   │   │   │   ├── printer/                      # Manajemen printer
│   │   │   │   ├── settings/                     # Pengaturan + Harga BBM
│   │   │   │   └── transaction/                  # Form transaksi
│   │   │   ├── util/
│   │   │   │   ├── BackupUtil.kt                 # Backup/restore DB
│   │   │   │   ├── ExportUtil.kt                 # PDF + Excel export
│   │   │   │   └── FormatUtil.kt                 # Format angka/tanggal
│   │   │   ├── MainActivity.kt
│   │   │   └── SPBUApplication.kt
│   │   ├── res/
│   │   │   ├── drawable/                         # Icon & vector
│   │   │   ├── mipmap-*/                         # App icon
│   │   │   ├── values/                           # strings, colors, themes
│   │   │   └── xml/                              # FileProvider, backup rules
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml                        # Version catalog
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## ❓ Troubleshooting

### Gradle Sync gagal
- Pastikan ada koneksi internet
- Coba `File → Invalidate Caches & Restart`
- Pastikan JDK 17 terinstall

### Printer tidak terdeteksi
- Pastikan Bluetooth HP sudah aktif
- Pair printer di Pengaturan Bluetooth sistem Android lebih dulu
- Izin Bluetooth harus disetujui saat diminta

### Error Permission Bluetooth di Android 12+
- Buka Pengaturan HP → Aplikasi → SPBU Receipt Printer → Izin → Aktifkan Bluetooth

### Tidak bisa cetak
- Pastikan printer terhubung (ada indikator hijau di dashboard)
- Coba Test Print dari menu Printer
- Pastikan kertas thermal sudah terpasang dengan benar

---

## 📞 Lisensi & Kredit

Project ini menggunakan library open source:
- **DantSu/ESCPOS-ThermalPrinter-Android** – MIT License
- **Apache POI** – Apache License 2.0
- **iText 7** – AGPL v3
- **ZXing** – Apache License 2.0

---

*Dibuat untuk memudahkan petugas SPBU Pertamina Indonesia* 🇮🇩
