// File: RunWaypoint.java (Perubahan di model)

package com.stridez.stridezbackend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "run_waypoints")
@Data
public class RunWaypoint {
    // ... (waypointId dan runId tidak diubah)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waypoint_id")
    private Long waypointId;

    @Column(name = "run_id")
    private Integer runId;

    // Field Wajib (harus diisi dari Flutter)
    @Column(name = "sequence", nullable = false)
    private Integer sequence; // TIDAK DIUBAH (Wajib diisi dari Flutter)

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp; // TIDAK DIUBAH (Wajib diisi dari Flutter)

    // Latitude / Longitude (Tipe BigDecimal, tapi biarkan non-nullable karena lokasi inti)
    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude; 

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    // --- PERBAIKAN: JADIKAN FIELD INI NULLABLE (Opsional) ---
    // Jika Flutter tidak mengirimkannya, Spring tidak akan menolak 400.
    
    @Column(name = "speed_mps", precision = 5, scale = 2)
    private BigDecimal speedMps; // TIDAK perlu nullable=false

    @Column(name = "altitude_meters", precision = 5, scale = 2)
    private BigDecimal altitudeMeters; // TIDAK perlu nullable=false

    @Column(name = "accel_x", precision = 5, scale = 2)
    private BigDecimal accelX; // ✅ Harus BigDecimal

    @Column(name = "gyro_z", precision = 5, scale = 2)
    private BigDecimal gyroZ;

    // Pastikan tidak ada tipe primitif (int, double) yang Anda gunakan, 
    // karena tipe primitif tidak boleh null. Semua sudah BigDecimal/Integer.
}