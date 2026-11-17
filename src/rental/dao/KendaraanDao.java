package rental.dao;

import java.nio.file.Path;

import rental.exception.DataTidakValidException;
import rental.model.Kendaraan;
import rental.model.KendaraanFactory;

public class KendaraanDao extends BaseCsvDao<Kendaraan> {
    public KendaraanDao(Path file) {
        super(file, "KDR");
        muat();
    }

    @Override
    protected Kendaraan dariCsv(String[] kolom) throws DataTidakValidException {
        return KendaraanFactory.dariCsv(kolom);
    }

    /** True bila plat sudah dipakai kendaraan lain (selain idKecuali). */
    public boolean platDipakai(String plat, String idKecuali) {
        for (Kendaraan k : data.values()) {
            if (k.getPlat().equalsIgnoreCase(plat.trim()) && !k.getId().equals(idKecuali)) return true;
        }
        return false;
    }
}
