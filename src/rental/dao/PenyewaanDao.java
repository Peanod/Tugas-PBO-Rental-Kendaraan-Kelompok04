package rental.dao;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import rental.exception.DataTidakValidException;
import rental.model.Kendaraan;
import rental.model.Pelanggan;
import rental.model.Penyewaan;
import rental.model.StatusSewa;

public class PenyewaanDao extends BaseCsvDao<Penyewaan> {
    private final KendaraanDao kendaraanDao;
    private final PelangganDao pelangganDao;

    public PenyewaanDao(Path file, KendaraanDao kendaraanDao, PelangganDao pelangganDao) {
        super(file, "SWA");
        this.kendaraanDao = kendaraanDao;
        this.pelangganDao = pelangganDao;
        muat();
    }

    /** Format CSV: id,idKendaraan,idPelanggan,tanggalMulai,lamaHari,asuransi,status */
    @Override
    protected Penyewaan dariCsv(String[] f) throws DataTidakValidException {
        if (f.length < 7) throw new DataTidakValidException("Baris data penyewaan rusak.");
        Kendaraan k = kendaraanDao.cari(f[1])
                .orElseThrow(() -> new DataTidakValidException("Kendaraan " + f[1] + " tidak ditemukan."));
        Pelanggan p = pelangganDao.cari(f[2])
                .orElseThrow(() -> new DataTidakValidException("Pelanggan " + f[2] + " tidak ditemukan."));
        try {
            Penyewaan s = new Penyewaan(f[0], k, p, LocalDate.parse(f[3]), Integer.parseInt(f[4]),
                    Boolean.parseBoolean(f[5]));
            s.setStatus(StatusSewa.valueOf(f[6]));
            return s;
        } catch (DateTimeParseException | IllegalArgumentException e) {
            throw new DataTidakValidException("Baris data penyewaan rusak (format tidak valid).");
        }
    }

    /** Menjaga referensi Kendaraan/Pelanggan selalu menunjuk objek terbaru dari DAO masing-masing. */
    @Override
    public List<Penyewaan> semua() {
        List<Penyewaan> daftar = super.semua();
        for (Penyewaan p : daftar) segarkanReferensi(p);
        return daftar;
    }

    @Override
    public Optional<Penyewaan> cari(String id) {
        Optional<Penyewaan> p = super.cari(id);
        p.ifPresent(this::segarkanReferensi);
        return p;
    }

    private void segarkanReferensi(Penyewaan p) {
        try {
            Optional<Kendaraan> k = kendaraanDao.cari(p.getKendaraan().getId());
            if (k.isPresent()) p.setKendaraan(k.get());
            Optional<Pelanggan> pl = pelangganDao.cari(p.getPelanggan().getId());
            if (pl.isPresent()) p.setPelanggan(pl.get());
        } catch (DataTidakValidException e) {
            throw new IllegalStateException(e); // tidak mungkin: nilai yang dipasang tidak pernah null
        }
    }

    public boolean adaKendaraan(String idKendaraan) {
        for (Penyewaan p : data.values()) if (p.getKendaraan().getId().equals(idKendaraan)) return true;
        return false;
    }

    public boolean adaPelanggan(String idPelanggan) {
        for (Penyewaan p : data.values()) if (p.getPelanggan().getId().equals(idPelanggan)) return true;
        return false;
    }
}
