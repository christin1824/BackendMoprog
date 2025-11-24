package com.stridez.stridezbackend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "runs")
@Data
public class Run {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "run_id")
    private Integer runId;

    // Foreign Key ke tabel users
    @Column(name = "user_id")
    private String userId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @PrePersist
    private void ensureStartTime() {
        if (this.startTime == null) {
            this.startTime = LocalDateTime.now();
        }
    }

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "total_distance_meters", precision = 10, scale = 2)
    private BigDecimal totalDistanceMeters;

    @Column(name = "avg_speed_kph", precision = 5, scale = 2)
    private BigDecimal avgSpeedKph;

    @Column(name = "calories_burned")
    private Integer caloriesBurned;

    @Column(name = "start_location_name", length = 150)
    private String startLocationName;

    /**
     * Optional geometry representation of the full route.
     *
     * Note: original design used PostGIS GEOMETRY(LineString,4326). Many local MySQL
     * installations don't support PostGIS; to keep compatibility we store the route
     * as WKT/GeoJSON text. If you run against a spatial-enabled database, you can
     * change the column type accordingly.
     */
    @Column(name = "run_geometry", columnDefinition = "TEXT")
    private String runGeometry;

}
