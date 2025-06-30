package com.leszko.calculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kelas ini adalah Controller. Tugasnya menerima permintaan web dari browser.
 * @RestController memberitahu Spring bahwa ini bukan controller biasa, tapi yang langsung mengembalikan data.
 */
@RestController
public class CalculatorController {

    // Spring akan secara otomatis "menyuntikkan" atau memberikan kita instance dari CalculatorService.
    // Ini seperti resepsionis yang punya nomor telepon langsung ke manajer operasional (service).
    @Autowired
    private CalculatorService calculator;

    /**
     * Ini adalah "papan petunjuk" di aplikasi kita.
     * @GetMapping("/add") berarti: "Jika ada permintaan ke alamat /add, jalankan metode di bawah ini."
     * @RequestParam mengambil nilai dari URL (misal: ?a=10&b=5)
     */
    @GetMapping("/add")
    public String add(@RequestParam String a, @RequestParam String b) {
        // Controller tidak berpikir. Ia hanya menyuruh Service untuk bekerja.
        return calculator.add(a, b);
    }
}