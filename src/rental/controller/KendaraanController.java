package rental.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import rental.dao.KendaraanDao;
import rental.dao.PenyewaanDao;
import rental.exception.DataTidakValidException;
import rental.model.Kendaraan;
import rental.model.KendaraanFactory;
import rental.util.Validasi;

/** Logika bisnis data kendaraan (CRUD + pencarian). View hanya memanggil method di sini. */
public class KendaraanController {
    private final KendaraanDao dao;
    private final PenyewaanDao penyewaanDao;

    public KendaraanController(KendaraanDao dao, PenyewaanDao penyewaanDao) {
        this.dao = dao;
        this.penyewaanDao = penyewaanDao;
    }

    /** jenis = null/"Semua" berarti tanpa filter jenis. */
    public List<Kendaraan> daftar(String kataKunci, String jenis) {
        String q = kataKunci == null ? "" : kataKunci.trim().toLowerCase();
        List<Kendaraan> hasil = new ArrayList<>();
        for (Kendaraan k : dao.semua()) {
            boolean cocokJenis = jenis == null || "Semua".equals(jenis) || k.getJenis().equals(jenis);
            boolean cocokTeks = q.isEmpty() || k.getPlat().toLowerCase().contains(q)
                    || k.getMerek().toLowerCase().contains(q) || k.getId().toLowerCase().contains(q);
            if (cocokJenis && cocokTeks) hasil.add(k);
        }
        return hasil;
    }

    public Kendaraan tambah(String jenis, String plat, String merek, String tarifTeks, int nilaiKhusus)
            throws DataTidakValidException {
        double tarif = Validasi.parseAngka(tarifTeks, "Tarif per hari");
        Kendaraan k = KendaraanFactory.buat(jenis, dao.idBerikut(), plat, merek, tarif, nilaiKhusus);
        if (dao.platDipakai(k.getPlat(), null)) {
            throw new DataTidakValidException("Plat nomor " + k.getPlat() + " sudah terdaftar.");
        }
        dao.tambah(k);
        return k;
    }

    public Kendaraan ubah(String id, String jenis, String plat, String merek, String tarifTeks, int nilaiKhusus)
            throws DataTidakValidException {
        Kendaraan lama = dao.cari(id).orElseThrow(() -> new DataTidakValidException("Kendaraan tidak ditemukan."));
        double tarif = Validasi.parseAngka(tarifTeks, "Tarif per hari");
        Kendaraan baru = KendaraanFactory.buat(jenis, id, plat, merek, tarif, nilaiKhusus);
        if (dao.platDipakai(baru.getPlat(), id)) {
            throw new DataTidakValidException("Plat nomor " + baru.getPlat() + " sudah terdaftar.");
        }
        baru.setTersedia(lama.isTersedia());
        dao.ubah(baru);
        return baru;
    }

    public void hapus(String id) throws DataTidakValidException {
        if (penyewaanDao.adaKendaraan(id)) {
            throw new DataTidakValidException(
                    "Kendaraan tidak dapat dihapus karena tercatat pada transaksi penyewaan.");
        }
        dao.hapus(id);
    }

    public int jumlahSemua() { return dao.semua().size(); }

    public int jumlahTersedia() {
        int n = 0;
        for (Kendaraan k : dao.semua()) if (k.isTersedia()) n++;
        return n;
    }

    public Optional<Kendaraan> cari(String id) { return dao.cari(id); }
}
