package com.stridez.stridezbackend.controller;

import com.stridez.stridezbackend.model.UserTotal;
import com.stridez.stridezbackend.repository.UserTotalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-totals")
public class UserTotalController {

    @Autowired
    private UserTotalRepository userTotalRepository;

    // ====================================================================
    // 1. POST/PUT: Mengupdate Statistik Total Pengguna
    // URL: POST http://localhost:8080/api/user-totals
    // Digunakan untuk membuat entry baru atau mengupdate total yang ada.
    // ====================================================================
    @PostMapping
    public ResponseEntity<UserTotal> createOrUpdateTotal(@RequestBody UserTotal newTotal) {
        // Cek apakah data total untuk user_id ini sudah ada
        return userTotalRepository.findById(newTotal.getUserId())
            .map(existingTotal -> {
                // Jika sudah ada, update nilai-nilai:
                existingTotal.setTotalRunsCount(newTotal.getTotalRunsCount());
                existingTotal.setTotalDurationSeconds(newTotal.getTotalDurationSeconds());
                existingTotal.setTotalDistanceMeters(newTotal.getTotalDistanceMeters());
                existingTotal.setTotalCaloriesBurned(newTotal.getTotalCaloriesBurned());
                // Simpan perubahan (akan menjadi PUT)
                return ResponseEntity.ok(userTotalRepository.save(existingTotal));
            })
            .orElseGet(() -> {
                // Jika belum ada, buat entry baru (akan menjadi POST)
                return ResponseEntity.ok(userTotalRepository.save(newTotal));
            });
    }

    // ====================================================================
    // 2. GET: Mengambil Statistik Total Pengguna (Dashboard/Akun)
    // URL: GET http://localhost:8080/api/user-totals/{userId}
    // ====================================================================
    @GetMapping("/{userId}")
    public ResponseEntity<UserTotal> getTotalByUserId(@PathVariable String userId) {
        return userTotalRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}