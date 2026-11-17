package rental.dao;

import java.nio.file.Path;

import rental.exception.DataTidakValidException;
import rental.model.Pelanggan;

public class PelangganDao extends BaseCsvDao<Pelanggan> {
    public PelangganDao(Path file) {
        super(file, "PLG");
        muat();
    }

    @Override
    protected Pelanggan dariCsv(String[] kolom) throws DataTidakValidException {
        return Pelanggan.dariCsv(kolom);
    }

    public boolean ktpDipakai(String noKtp, String idKecuali) {
        for (Pelanggan p : data.values()) {
            if (p.getNoKtp().equals(noKtp.trim()) && !p.getId().equals(idKecuali)) return true;
        }
        return false;
    }
}
