package rental;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import rental.controller.KendaraanController;
import rental.controller.PelangganController;
import rental.controller.PenyewaanController;
import rental.dao.DataAwal;
import rental.dao.KendaraanDao;
import rental.dao.PelangganDao;
import rental.dao.PenyewaanDao;
import rental.exception.DataTidakValidException;
import rental.model.Kendaraan;
import rental.model.Pelanggan;
import rental.model.Penyewaan;

/**
 * Uji lewat KONSOL (titik pemeriksaan Minggu 2): model, polimorfisme, DAO, dan exception
 * dapat dijalankan tanpa GUI. Memakai folder sementara sehingga data asli tidak terganggu.
 */
public class ConsoleDemo {
    public static void main(String[] args) throws Exception {
        Path tmp = Files.createTempDirectory("rental-demo");
        KendaraanDao kDao = new KendaraanDao(tmp.resolve("kendaraan.csv"));
        PelangganDao pDao = new PelangganDao(tmp.resolve("pelanggan.csv"));
        PenyewaanDao sDao = new PenyewaanDao(tmp.resolve("penyewaan.csv"), kDao, pDao);
        DataAwal.isiJikaKosong(kDao, pDao);

        KendaraanController kc = new KendaraanController(kDao, sDao);
        PelangganController pc = new PelangganController(pDao, sDao);
        PenyewaanController sc = new PenyewaanController(sDao, kDao, pDao);

        System.out.println("=== 1. POLIMORFISME: hitungSewa() 7 hari per jenis ===");
        for (Kendaraan k : kc.daftar("", "Semua")) { // referensi bertipe induk Kendaraan
            System.out.printf("%-8s %-24s -> %,.0f%n", k.getJenis(), k.getMerek(), k.hitungSewa(7));
        }

        System.out.println("\n=== 2. OVERLOADING: hitungSewa(hari, asuransi) ===");
        Kendaraan avanza = kc.daftar("avanza", "Semua").get(0);
        System.out.printf("%s 3 hari tanpa asuransi: %,.0f | dengan asuransi: %,.0f%n",
                avanza.getMerek(), avanza.hitungSewa(3), avanza.hitungSewa(3, true));

        System.out.println("\n=== 3. CRUD kendaraan & pelanggan ===");
        Kendaraan baru = kc.tambah("Motor", "h 1111 zz", "Suzuki Satria", "120.000", 150);
        System.out.println("Tambah : " + baru);
        kc.ubah(baru.getId(), "Motor", "H 1111 ZZ", "Suzuki Satria", "130000", 150);
        System.out.println("Ubah   : tarif sekarang " + kDao.cari(baru.getId()).get().getTarifPerHari());
        kc.hapus(baru.getId());
        System.out.println("Hapus  : sisa kendaraan = " + kc.jumlahSemua());

        System.out.println("\n=== 4. Transaksi sewa ===");
        List<Pelanggan> plg = pc.daftar("");
        Penyewaan s = sc.sewa(avanza, plg.get(0), LocalDate.now(), 3, true);
        System.out.printf("%s | %s menyewa %s | total %,.0f | tersedia? %b%n", s.getId(),
                s.getPelanggan().getNama(), s.getKendaraan().getMerek(), s.hitungTotal(), avanza.isTersedia());
        sc.selesaikan(s.getId());
        System.out.printf("Selesai | tersedia? %b | pendapatan %,.0f%n", avanza.isTersedia(), sc.totalPendapatan());

        System.out.println("\n=== 5. EXCEPTION HANDLING ===");
        coba(() -> kc.tambah("Mobil", "", "Xenia", "300000", 5));
        coba(() -> kc.tambah("Mobil", "H 9 X", "Xenia", "-5", 5));
        coba(() -> kc.tambah("Mobil", "H 9 X", "Xenia", "abc", 5));
        coba(() -> pc.tambah("Dewi", "123", "0812345678", "Semarang", "P"));
        coba(() -> kc.hapus(avanza.getId()));

        System.out.println("\n=== 6. PERSISTENSI: data dibaca ulang dari file ===");
        KendaraanDao ulang = new KendaraanDao(tmp.resolve("kendaraan.csv"));
        System.out.println("Kendaraan terbaca dari file: " + ulang.semua().size());
    }

    private interface Aksi { void jalan() throws DataTidakValidException; }

    private static void coba(Aksi aksi) {
        try {
            aksi.jalan();
            System.out.println("(tidak ada kesalahan)");
        } catch (DataTidakValidException e) {
            System.out.println("DataTidakValidException -> " + e.getMessage());
        }
    }
}
