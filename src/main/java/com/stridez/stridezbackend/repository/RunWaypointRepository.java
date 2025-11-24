package com.stridez.stridezbackend.repository;

import com.stridez.stridezbackend.model.RunWaypoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RunWaypointRepository extends JpaRepository<RunWaypoint, Long> {
    // Digunakan untuk mengambil semua titik koordinat untuk sesi lari tertentu
    List<RunWaypoint> findByRunId(Integer runId);
}
