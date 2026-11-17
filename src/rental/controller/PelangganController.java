package rental.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import rental.dao.PelangganDao;
import rental.dao.PenyewaanDao;
import rental.exception.DataTidakValidException;
import rental.model.Pelanggan;

/** Logika bisnis data pelanggan. */
public class PelangganController {
    private final PelangganDao dao;
    private final PenyewaanDao penyewaanDao;

    public PelangganController(PelangganDao dao, PenyewaanDao penyewaanDao) {
        this.dao = dao;
        this.penyewaanDao = penyewaanDao;
    }

    public List<Pelanggan> daftar(String kataKunci) {
        String q = kataKunci == null ? "" : kataKunci.trim().toLowerCase();
        List<Pelanggan> hasil = new ArrayList<>();
        for (Pelanggan p : dao.semua()) {
            if (q.isEmpty() || p.getNama().toLowerCase().contains(q) || p.getNoKtp().contains(q)
                    || p.getTelepon().contains(q) || p.getId().toLowerCase().contains(q)) {
                hasil.add(p);
            }
        }
        return hasil;
    }

    public Pelanggan tambah(String nama, String noKtp, String telepon, String alamat, String jk)
            throws DataTidakValidException {
        Pelanggan p = new Pelanggan(dao.idBerikut(), nama, noKtp, telepon, alamat, jk);
        if (dao.ktpDipakai(p.getNoKtp(), null)) {
            throw new DataTidakValidException("No. KTP sudah terdaftar atas nama pelanggan lain.");
        }
        dao.tambah(p);
        return p;
    }

    public Pelanggan ubah(String id, String nama, String noKtp, String telepon, String alamat, String jk)
            throws DataTidakValidException {
        Pelanggan p = dao.cari(id).orElseThrow(() -> new DataTidakValidException("Pelanggan tidak ditemukan."));
        p.setNama(nama);
        p.setNoKtp(noKtp);
        p.setTelepon(telepon);
        p.setAlamat(alamat);
        p.setJenisKelamin(jk);
        if (dao.ktpDipakai(p.getNoKtp(), id)) {
            throw new DataTidakValidException("No. KTP sudah terdaftar atas nama pelanggan lain.");
        }
        dao.ubah(p);
        return p;
    }

    public void hapus(String id) throws DataTidakValidException {
        if (penyewaanDao.adaPelanggan(id)) {
            throw new DataTidakValidException(
                    "Pelanggan tidak dapat dihapus karena tercatat pada transaksi penyewaan.");
        }
        dao.hapus(id);
    }

    public List<Pelanggan> semua() { return dao.semua(); }

    public Optional<Pelanggan> cari(String id) { return dao.cari(id); }
}
