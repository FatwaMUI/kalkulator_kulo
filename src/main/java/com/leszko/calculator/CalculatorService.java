package com.leszko.calculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Kelas ini adalah Service. Tugasnya melakukan semua "pekerjaan berat" atau logika bisnis.
 * @Service memberitahu Spring bahwa ini adalah komponen logika utama.
 */
@Service
public class CalculatorService {

    // Service menyuruh Repository untuk menyimpan data.
    // Ini seperti manajer yang menyuruh staf gudang (repository) untuk mencatat barang.
    @Autowired
    private CalculationRepository repository;

    /**
     * Metode ini berisi logika untuk menjumlahkan dan menyimpan ke database.
     */
    public String add(String a, String b) {
        // 1. Lakukan perhitungan
        int sum = Integer.parseInt(a) + Integer.parseInt(b);
        String result = String.valueOf(sum);

        // 2. Buat objek data baru menggunakan "cetakan" Calculation.java
        Calculation calculation = new Calculation(a, b, result);

        // 3. Suruh repository untuk menyimpan objek data ini ke database
        repository.save(calculation);

        // 4. Kembalikan hasilnya untuk ditampilkan di browser
        return result;
    }
}