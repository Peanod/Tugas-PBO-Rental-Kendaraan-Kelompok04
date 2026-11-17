package rental.dao;

import java.nio.file.Path;

import rental.exception.DataTidakValidException;
import rental.model.Pengguna;

public class PenggunaDao extends BaseCsvDao<Pengguna> {
    public PenggunaDao(Path file) {
        super(file, "USR");
        muat();
        if (data.isEmpty()) isiAkunAwal();
    }

    @Override
    protected Pengguna dariCsv(String[] kolom) throws DataTidakValidException {
        return Pengguna.dariCsv(kolom);
    }

    private void isiAkunAwal() {
        try {
            Pengguna admin = Pengguna.baru("admin", "admin123", "Administrator", "ADMIN");
            Pengguna petugas = Pengguna.baru("petugas", "petugas123", "Petugas Rental", "PETUGAS");
            data.put(admin.getId(), admin);
            data.put(petugas.getId(), petugas);
            simpan();
        } catch (DataTidakValidException e) {
            throw new IllegalStateException(e);
        }
    }
}
