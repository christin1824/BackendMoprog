package com.stridez.stridezbackend.repository;

import com.stridez.stridezbackend.model.Run;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RunRepository extends JpaRepository<Run, Integer> {
    // Digunakan untuk mengambil riwayat lari (Riwayat Lari Anda)
    List<Run> findByUserId(String userId);
}