# Rental Kendaraan - Tugas Proyek PBO (Implementasi GUI) 2026

**Kelompok 4 - Tema: Rental Kendaraan**
Anggota: Finodi, I Dewa Gede Agung Oka, Lionel Jeremy

Aplikasi desktop Java Swing untuk mengelola rental Mobil, Motor, dan Bus: data kendaraan, data
pelanggan, dan transaksi penyewaan. Arsitektur MVC, data tersimpan permanen di file (folder `data/`).

---

## 1. Aplikasi yang harus dipasang

| Kebutuhan | Keterangan |
| --- | --- |
| **JDK 17 atau 21** | Unduh *Temurin (Adoptium)* di https://adoptium.net. Kode kompatibel mulai Java 11. |
| **Eclipse IDE for Java Developers** | Unduh di https://www.eclipse.org/downloads (sesuai ketentuan tugas). |
| Git (opsional tapi wajib untuk tugas) | https://git-scm.com |

Tidak ada library tambahan (tanpa Maven/Gradle, tanpa file .jar). Penyimpanan memakai file teks.

## 2. Cara menjalankan di Eclipse (disarankan)

1. Ekstrak `rental-kendaraan.zip` ke sebuah folder.
2. Buka Eclipse, lalu **File > Import... > General > Existing Projects into Workspace > Next**.
3. Pada *Select root directory* klik **Browse**, pilih folder hasil ekstrak (yang berisi `src` dan `.project`),
   pastikan proyek **RentalKendaraan** tercentang, klik **Finish**.
4. Jika Eclipse meminta JRE: klik kanan proyek > **Properties > Java Build Path > Libraries** dan pastikan
   JRE System Library memakai JDK 17/21 yang terpasang.
5. Buka `src/rental/Main.java`, klik kanan > **Run As > Java Application**.
6. Login dengan akun demo:

| Username | Kata sandi | Peran |
| --- | --- | --- |
| `admin` | `admin123` | ADMIN |
| `petugas` | `petugas123` | PETUGAS |

Untuk uji lewat **konsol** (titik pemeriksaan Minggu 2): jalankan `src/rental/ConsoleDemo.java` dengan
**Run As > Java Application**. Program ini mencetak demo polimorfisme, CRUD, exception, dan persistensi
memakai folder sementara (data asli tidak terganggu).

## 3. Cara menjalankan tanpa IDE

```
# Linux / macOS
./run.sh

# Windows
run.bat
```

(Butuh `java` dan `javac` ada di PATH: cek dengan `javac -version`.)

## 4. Struktur proyek

```
src/rental/
  Main.java, AppContext.java, ConsoleDemo.java
  model/       Kendaraan (abstract), Mobil, Motor, Bus, KendaraanFactory, Pelanggan,
               Penyewaan, StatusSewa (enum), Pengguna, DapatDisimpan (interface)
  exception/   DataTidakValidException (custom), PenyimpananException
  util/        Validasi, CsvUtil, Format
  dao/         Dao<T> (interface generik), BaseCsvDao<T> (abstract), KendaraanDao,
               PelangganDao, PenyewaanDao, PenggunaDao, DataAwal
  controller/  AuthController, KendaraanController, PelangganController, PenyewaanController
  view/        LoginFrame, MainFrame, DashboardPanel, KendaraanPanel, PelangganPanel,
               PenyewaanPanel, PanelData (abstract), UiUtil
data/          dibuat otomatis saat pertama dijalankan (kendaraan.csv, pelanggan.csv, ...)
```

## 5. Pemetaan ke ketentuan tugas

| Ketentuan | Letak di kode |
| --- | --- |
| Min. 8 kelas model/logika | 10 kelas/enum/interface model + 4 controller + 5 DAO + util |
| Enkapsulasi + validasi | Semua atribut `private`; setter memvalidasi lewat `util.Validasi` (mis. tarif tidak boleh negatif/nol, KTP 16 digit) |
| Pewarisan (1 induk, 2+ turunan, `super`) | `Kendaraan` -> `Mobil`, `Motor`, `Bus` (konstruktor memanggil `super(...)`) |
| Abstract class + method abstrak | `Kendaraan.hitungSewa(int)`, `getJenis()`, `getDetail()`; juga `BaseCsvDao`, `PanelData` |
| Interface (>1 implementor) | `DapatDisimpan` diimplementasikan `Kendaraan`, `Pelanggan`, `Penyewaan`, `Pengguna` |
| Overriding via referensi induk | `hitungSewa()` berbeda per jenis; dipanggil lewat `Kendaraan` di `Penyewaan.hitungTotal()` |
| Overloading | `Kendaraan.hitungSewa(int)` dan `hitungSewa(int, boolean)` |
| Collection & generics | `Map<String,T>` di `BaseCsvDao<T>`, `List<Kendaraan>`, `Dao<T extends DapatDisimpan>` |
| Exception handling + custom | `DataTidakValidException`; try-catch I/O di `BaseCsvDao` (dibungkus `PenyimpananException`) dan di `PanelData.jalankan()` |
| Min. 3 tampilan + login | `LoginFrame`, `MainFrame`, 4 panel (Dashboard, Kendaraan, Pelanggan, Penyewaan) |
| CRUD >= 2 entitas dalam JTable | Kendaraan, Pelanggan, dan Penyewaan (3 entitas) |
| Input bervariasi | JTextField, JComboBox, JRadioButton (jenis kelamin), JCheckBox (asuransi), JSpinner (tanggal & jumlah) |
| Menu bar / navigasi konsisten | `JMenuBar` + `JToolBar` di `MainFrame` (Ctrl+1..4) |
| Dialog konfirmasi hapus + pesan error | `UiUtil.konfirmasi()` dan `UiUtil.galat()` |
| Event handling tidak menumpuk | Tiap tombol memanggil method sendiri (`aksiTambah`, `aksiUbah`, `aksiHapus`, ...) |
| Layout manager | `BorderLayout`, `GridBagLayout`, `GridLayout`, `CardLayout`; tampilan tetap rapi saat di-resize |
| MVC | `view` hanya memanggil `controller`; `controller` memakai `dao` dan `model` |
| Pencarian | Kolom "Cari" + filter pada setiap halaman data |

## 6. Aturan tarif (polimorfisme `hitungSewa`)

- **Mobil**: tarif x hari, diskon 10% bila sewa >= 7 hari.
- **Motor**: tarif x hari, diskon 5% bila sewa >= 3 hari.
- **Bus**: (tarif + biaya sopir Rp250.000) x hari, tanpa diskon.
- **Asuransi** (opsional, semua jenis): +5% dari biaya sewa.

## 7. Aturan bisnis lain

- Kendaraan yang sedang disewa tidak bisa disewa lagi; otomatis tersedia lagi saat transaksi diselesaikan/dihapus.
- Kendaraan atau pelanggan yang sudah tercatat di transaksi tidak bisa dihapus (menjaga riwayat).
- Plat nomor dan No. KTP harus unik. Transaksi yang sudah selesai tidak bisa diubah.

## 8. Pembagian kerja & Git (saran)

Ketentuan tugas: **setiap anggota wajib punya commit pada bagian yang menjadi tanggung jawabnya.**
Saran pembagian (boleh ditukar sesuai kesepakatan kelompok):

| Anggota | Tanggung jawab | Paket |
| --- | --- | --- |
| Finodi | Model, hierarki pewarisan, exception, util | `model`, `exception`, `util` |
| I Dewa Gede Agung Oka | DAO, controller, uji konsol, perakitan | `dao`, `controller`, `AppContext`, `ConsoleDemo` |
| Lionel Jeremy | Seluruh tampilan GUI | `view`, `Main` |


## 9. Pertanyaan umum

- **Data hilang / ingin reset?** Tutup aplikasi, hapus folder `data/`, jalankan lagi (data contoh dibuat ulang).
- **Eclipse bilang "Unbound JRE"?** Lihat langkah 4 di bagian Eclipse.
- **Ingin pakai database SQLite/MySQL?** Cukup tambah kelas DAO baru yang mengimplementasikan `Dao<T>`;
  controller dan view tidak perlu diubah (itulah gunanya lapisan DAO).
