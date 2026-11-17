package rental.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import rental.exception.DataTidakValidException;
import rental.exception.PenyimpananException;
import rental.model.DapatDisimpan;
import rental.util.CsvUtil;

/**
 * Implementasi dasar DAO berbasis file teks. Data disimpan di Map (collection generik)
 * dan ditulis ulang ke file setiap ada perubahan.
 */
public abstract class BaseCsvDao<T extends DapatDisimpan> implements Dao<T> {
    private final Path file;
    private final String prefixId;
    protected final Map<String, T> data = new LinkedHashMap<>();

    protected BaseCsvDao(Path file, String prefixId) {
        this.file = file;
        this.prefixId = prefixId;
    }

    /** Mengubah satu baris file menjadi objek entitas. */
    protected abstract T dariCsv(String[] kolom) throws DataTidakValidException;

    /** Dipanggil oleh konstruktor subkelas setelah semua dependensinya siap. */
    protected void muat() {
        data.clear();
        if (!Files.exists(file)) return;
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String baris;
            while ((baris = r.readLine()) != null) {
                if (baris.trim().isEmpty()) continue;
                try {
                    T t = dariCsv(CsvUtil.pisah(baris));
                    data.put(t.getId(), t);
                } catch (DataTidakValidException e) {
                    System.err.println("[" + file.getFileName() + "] baris dilewati: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new PenyimpananException("Gagal membaca file " + file, e);
        }
    }

    protected void simpan() {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                for (T t : data.values()) {
                    w.write(t.toCsv());
                    w.newLine();
                }
            }
        } catch (IOException e) {
            throw new PenyimpananException("Gagal menyimpan file " + file, e);
        }
    }

    @Override
    public List<T> semua() {
        return new ArrayList<>(data.values());
    }

    @Override
    public Optional<T> cari(String id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public void tambah(T entitas) throws DataTidakValidException {
        if (data.containsKey(entitas.getId())) {
            throw new DataTidakValidException("ID " + entitas.getId() + " sudah digunakan.");
        }
        data.put(entitas.getId(), entitas);
        simpan();
    }

    @Override
    public void ubah(T entitas) throws DataTidakValidException {
        if (!data.containsKey(entitas.getId())) {
            throw new DataTidakValidException("Data dengan ID " + entitas.getId() + " tidak ditemukan.");
        }
        data.put(entitas.getId(), entitas);
        simpan();
    }

    @Override
    public void hapus(String id) throws DataTidakValidException {
        if (data.remove(id) == null) {
            throw new DataTidakValidException("Data dengan ID " + id + " tidak ditemukan.");
        }
        simpan();
    }

    @Override
    public String idBerikut() {
        int maks = 0;
        for (String id : data.keySet()) {
            if (id.startsWith(prefixId)) {
                try {
                    maks = Math.max(maks, Integer.parseInt(id.substring(prefixId.length())));
                } catch (NumberFormatException ignored) {
                    // ID dengan format lain diabaikan
                }
            }
        }
        return String.format("%s%03d", prefixId, maks + 1);
    }
}
