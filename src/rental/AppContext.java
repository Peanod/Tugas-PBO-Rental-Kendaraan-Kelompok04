package rental;

import java.nio.file.Path;

import rental.controller.AuthController;
import rental.controller.KendaraanController;
import rental.controller.PelangganController;
import rental.controller.PenyewaanController;
import rental.dao.DataAwal;
import rental.dao.KendaraanDao;
import rental.dao.PelangganDao;
import rental.dao.PenggunaDao;
import rental.dao.PenyewaanDao;

/** Merakit seluruh DAO dan controller sekali saja, lalu dibagikan ke semua tampilan. */
public class AppContext {
    public final AuthController auth;
    public final KendaraanController kendaraan;
    public final PelangganController pelanggan;
    public final PenyewaanController penyewaan;

    public AppContext(Path folderData) {
        PenggunaDao penggunaDao = new PenggunaDao(folderData.resolve("pengguna.csv"));
        KendaraanDao kendaraanDao = new KendaraanDao(folderData.resolve("kendaraan.csv"));
        PelangganDao pelangganDao = new PelangganDao(folderData.resolve("pelanggan.csv"));
        PenyewaanDao penyewaanDao = new PenyewaanDao(folderData.resolve("penyewaan.csv"), kendaraanDao, pelangganDao);
        DataAwal.isiJikaKosong(kendaraanDao, pelangganDao);

        auth = new AuthController(penggunaDao);
        kendaraan = new KendaraanController(kendaraanDao, penyewaanDao);
        pelanggan = new PelangganController(pelangganDao, penyewaanDao);
        penyewaan = new PenyewaanController(penyewaanDao, kendaraanDao, pelangganDao);
    }
}
