package rental.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import rental.dao.KendaraanDao;
import rental.dao.PelangganDao;
import rental.dao.PenyewaanDao;
import rental.exception.DataTidakValidException;
import rental.model.Kendaraan;
import rental.model.Pelanggan;
import rental.model.Penyewaan;
import rental.model.StatusSewa;

/** Logika bisnis transaksi penyewaan: mulai sewa, ubah, selesaikan, hapus. */
public class PenyewaanController {
    private final PenyewaanDao dao;
    private final KendaraanDao kendaraanDao;
    private final PelangganDao pelangganDao;

    public PenyewaanController(PenyewaanDao dao, KendaraanDao kendaraanDao, PelangganDao pelangganDao) {
        this.dao = dao;
        this.kendaraanDao = kendaraanDao;
        this.pelangganDao = pelangganDao;
    }

    public List<Penyewaan> daftar(String kataKunci, StatusSewa status) {
        String q = kataKunci == null ? "" : kataKunci.trim().toLowerCase();
        List<Penyewaan> hasil = new ArrayList<>();
        for (Penyewaan p : dao.semua()) {
            boolean cocokStatus = status == null || p.getStatus() == status;
            boolean cocokTeks = q.isEmpty() || p.getId().toLowerCase().contains(q)
                    || p.getPelanggan().getNama().toLowerCase().contains(q)
                    || p.getKendaraan().getPlat().toLowerCase().contains(q)
                    || p.getKendaraan().getMerek().toLowerCase().contains(q);
            if (cocokStatus && cocokTeks) hasil.add(p);
        }
        return hasil;
    }

    public List<Kendaraan> kendaraanTersedia() {
        List<Kendaraan> hasil = new ArrayList<>();
        for (Kendaraan k : kendaraanDao.semua()) if (k.isTersedia()) hasil.add(k);
        return hasil;
    }

    public List<Pelanggan> semuaPelanggan() { return pelangganDao.semua(); }

    /** Estimasi biaya (dipakai untuk label di form) - memanfaatkan polimorfisme + overloading. */
    public double estimasi(Kendaraan k, int hari, boolean asuransi) {
        return k == null ? 0 : k.hitungSewa(hari, asuransi);
    }

    public Penyewaan sewa(Kendaraan kendaraan, Pelanggan pelanggan, LocalDate mulai, int hari, boolean asuransi)
            throws DataTidakValidException {
        if (kendaraan == null) throw new DataTidakValidException("Kendaraan harus dipilih.");
        Kendaraan k = kendaraanDao.cari(kendaraan.getId())
                .orElseThrow(() -> new DataTidakValidException("Kendaraan tidak ditemukan."));
        if (!k.isTersedia()) throw new DataTidakValidException("Kendaraan " + k.getPlat() + " sedang disewa.");
        Penyewaan p = new Penyewaan(dao.idBerikut(), k, pelanggan, mulai, hari, asuransi);
        dao.tambah(p);
        k.setTersedia(false);
        kendaraanDao.ubah(k);
        return p;
    }

    public Penyewaan ubah(String id, Pelanggan pelanggan, LocalDate mulai, int hari, boolean asuransi)
            throws DataTidakValidException {
        Penyewaan p = dao.cari(id).orElseThrow(() -> new DataTidakValidException("Transaksi tidak ditemukan."));
        if (p.getStatus() != StatusSewa.AKTIF) {
            throw new DataTidakValidException("Transaksi yang sudah selesai tidak dapat diubah.");
        }
        p.setPelanggan(pelanggan);
        p.setTanggalMulai(mulai);
        p.setLamaHari(hari);
        p.setAsuransi(asuransi);
        dao.ubah(p);
        return p;
    }

    public void selesaikan(String id) throws DataTidakValidException {
        Penyewaan p = dao.cari(id).orElseThrow(() -> new DataTidakValidException("Transaksi tidak ditemukan."));
        if (p.getStatus() == StatusSewa.SELESAI) {
            throw new DataTidakValidException("Transaksi ini sudah selesai.");
        }
        p.setStatus(StatusSewa.SELESAI);
        dao.ubah(p);
        bebaskanKendaraan(p);
    }

    public void hapus(String id) throws DataTidakValidException {
        Penyewaan p = dao.cari(id).orElseThrow(() -> new DataTidakValidException("Transaksi tidak ditemukan."));
        dao.hapus(id);
        if (p.getStatus() == StatusSewa.AKTIF) bebaskanKendaraan(p);
    }

    private void bebaskanKendaraan(Penyewaan p) throws DataTidakValidException {
        Kendaraan k = p.getKendaraan();
        k.setTersedia(true);
        kendaraanDao.ubah(k);
    }

    public int jumlahAktif() {
        int n = 0;
        for (Penyewaan p : dao.semua()) if (p.getStatus() == StatusSewa.AKTIF) n++;
        return n;
    }

    public double totalPendapatan() {
        double total = 0;
        for (Penyewaan p : dao.semua()) if (p.getStatus() == StatusSewa.SELESAI) total += p.hitungTotal();
        return total;
    }

    public Optional<Penyewaan> cari(String id) { return dao.cari(id); }
}
