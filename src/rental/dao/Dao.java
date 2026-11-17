package rental.dao;

import java.util.List;
import java.util.Optional;

import rental.exception.DataTidakValidException;
import rental.model.DapatDisimpan;

/** Interface generik untuk akses data (CRUD). T adalah tipe entitas. */
public interface Dao<T extends DapatDisimpan> {
    List<T> semua();

    Optional<T> cari(String id);

    void tambah(T entitas) throws DataTidakValidException;

    void ubah(T entitas) throws DataTidakValidException;

    void hapus(String id) throws DataTidakValidException;

    /** Menghasilkan ID unik berikutnya, mis. KDR001, KDR002, ... */
    String idBerikut();
}
