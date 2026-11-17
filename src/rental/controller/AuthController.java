package rental.controller;

import java.util.Optional;

import rental.dao.PenggunaDao;
import rental.exception.DataTidakValidException;
import rental.model.Pengguna;
import rental.util.Validasi;

/** Mengurus proses login/logout dan menyimpan pengguna yang sedang aktif. */
public class AuthController {
    private final PenggunaDao penggunaDao;
    private Pengguna penggunaAktif;

    public AuthController(PenggunaDao penggunaDao) {
        this.penggunaDao = penggunaDao;
    }

    public Pengguna masuk(String username, String sandi) throws DataTidakValidException {
        String u = Validasi.wajibIsi(username, "Username");
        Validasi.wajibIsi(sandi, "Kata sandi");
        Optional<Pengguna> p = penggunaDao.cari(u);
        if (!p.isPresent() || !p.get().cocok(sandi)) {
            throw new DataTidakValidException("Username atau kata sandi salah.");
        }
        penggunaAktif = p.get();
        return penggunaAktif;
    }

    public void keluar() { penggunaAktif = null; }

    public Pengguna getPenggunaAktif() { return penggunaAktif; }
}
