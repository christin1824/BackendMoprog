package com.stridez.stridezbackend.controller;

import com.stridez.stridezbackend.model.DailyStat;
import com.stridez.stridezbackend.repository.DailyStatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/daily-stats")
public class DailyStatController {

    @Autowired
    private DailyStatRepository dailyStatRepository;

    // ====================================================================
    // 1. POST/PUT: Menyimpan atau Mengupdate Statistik Harian
    // URL: POST http://localhost:8080/api/daily-stats
    // ====================================================================
    @PostMapping
    public ResponseEntity<DailyStat> saveOrUpdateDailyStat(@RequestBody DailyStat newStat) {
        // Menggunakan method kustom findByUserIdAndDate untuk memastikan tidak ada duplikasi
        Optional<DailyStat> existingStat = dailyStatRepository.findByUserIdAndDate(newStat.getUserId(), newStat.getDate());

        if (existingStat.isPresent()) {
            // Jika sudah ada (UPDATE)
            DailyStat statToUpdate = existingStat.get();
            // Perbarui semua kolom yang bisa diubah
            statToUpdate.setTotalSteps(newStat.getTotalSteps());
            statToUpdate.setTotalDistanceRun(newStat.getTotalDistanceRun());
            statToUpdate.setCaloriesBurnedRun(newStat.getCaloriesBurnedRun());
            statToUpdate.setCaloriesBurnedSteps(newStat.getCaloriesBurnedSteps());
            return ResponseEntity.ok(dailyStatRepository.save(statToUpdate));
        } else {
            // Jika belum ada (CREATE)
            return ResponseEntity.ok(dailyStatRepository.save(newStat));
        }
    }

    // ====================================================================
    // 2. GET: Mengambil Statistik Harian Spesifik
    // URL: GET http://localhost:8080/api/daily-stats/{userId}/{date}
    // ====================================================================
    @GetMapping("/{userId}/{date}")
    public ResponseEntity<DailyStat> getDailyStatByDate(
            @PathVariable String userId,
            @PathVariable LocalDate date) {
        
        // Mengambil statistik berdasarkan user_id dan tanggal dari URL
        return dailyStatRepository.findByUserIdAndDate(userId, date)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}