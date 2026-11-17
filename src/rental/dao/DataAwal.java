package rental.dao;

import rental.exception.DataTidakValidException;
import rental.model.Kendaraan;
import rental.model.KendaraanFactory;
import rental.model.Pelanggan;

/** Mengisi data contoh saat aplikasi pertama kali dijalankan (agar demo tidak kosong). */
public final class DataAwal {
    private DataAwal() { }

    public static void isiJikaKosong(KendaraanDao kendaraanDao, PelangganDao pelangganDao) {
        try {
            if (kendaraanDao.semua().isEmpty()) {
                Object[][] k = {
                    { "Mobil", "H 1234 AB", "Toyota Avanza", 350_000.0, 7 },
                    { "Mobil", "H 5678 CD", "Honda Brio", 300_000.0, 5 },
                    { "Mobil", "H 9012 EF", "Toyota Innova Zenix", 650_000.0, 7 },
                    { "Motor", "H 3456 GH", "Honda Vario 160", 100_000.0, 160 },
                    { "Motor", "H 7890 IJ", "Yamaha NMAX", 150_000.0, 155 },
                    { "Motor", "H 2468 KL", "Honda Beat", 75_000.0, 110 },
                    { "Bus", "H 1357 MN", "Hino Medium Bus", 1_500_000.0, 35 },
                    { "Bus", "H 8642 OP", "Mercedes-Benz OH 1626", 2_800_000.0, 59 },
                };
                for (Object[] r : k) {
                    Kendaraan kd = KendaraanFactory.buat((String) r[0], kendaraanDao.idBerikut(), (String) r[1],
                            (String) r[2], (Double) r[3], (Integer) r[4]);
                    kendaraanDao.tambah(kd);
                }
            }
            if (pelangganDao.semua().isEmpty()) {
                String[][] p = {
                    { "Budi Santoso", "3374010101900001", "081234567890", "Jl. Pandanaran No. 10, Semarang", "L" },
                    { "Siti Rahayu", "3374014502920002", "081298765432", "Jl. Pemuda No. 25, Semarang", "P" },
                    { "Ahmad Fauzi", "3374012003880003", "085712345678", "Jl. Setiabudi No. 7, Semarang", "L" },
                };
                for (String[] r : p) {
                    pelangganDao.tambah(new Pelanggan(pelangganDao.idBerikut(), r[0], r[1], r[2], r[3], r[4]));
                }
            }
        } catch (DataTidakValidException e) {
            throw new IllegalStateException("Data awal tidak valid: " + e.getMessage(), e);
        }
    }
}
