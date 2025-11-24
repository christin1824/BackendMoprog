package com.stridez.stridezbackend.repository;

import com.stridez.stridezbackend.model.DailyStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyStatRepository extends JpaRepository<DailyStat, Long> {
    // Digunakan untuk mengambil atau mengupdate statistik spesifik untuk hari itu
    Optional<DailyStat> findByUserIdAndDate(String userId, LocalDate date);
}