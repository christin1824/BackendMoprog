package com.stridez.stridezbackend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "run_waypoints")
@Data
public class RunWaypoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waypoint_id")
    private Long waypointId;

    // Foreign Key ke tabel runs
    @Column(name = "run_id")
    private Integer runId;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // Latitude / Longitude for the waypoint (WGS84)
    // Precision/scale chosen to allow ~7 decimal places (~1cm precision). Adjust if needed.
    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "speed_mps", precision = 5, scale = 2)
    private BigDecimal speedMps;

    @Column(name = "altitude_meters", precision = 5, scale = 2)
    private BigDecimal altitudeMeters;

    @Column(name = "accel_x", precision = 5, scale = 2)
    private BigDecimal accelX;

    @Column(name = "gyro_z", precision = 5, scale = 2)
    private BigDecimal gyroZ;


}
