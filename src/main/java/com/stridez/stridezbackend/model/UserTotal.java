package com.stridez.stridezbackend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "user_totals")
@Data
public class UserTotal {

    @Id
    @Column(name = "user_id", length = 255)
    private String userId;

    @ColumnDefault("0")
    @Column(name = "total_runs_count")
    private Integer totalRunsCount;

    @ColumnDefault("0")
    @Column(name = "total_duration_seconds")
    private Integer totalDurationSeconds;

    @ColumnDefault("0.00")
    @Column(name = "total_distance_meters", precision = 10, scale = 2)
    private BigDecimal totalDistanceMeters;

    @ColumnDefault("0")
    @Column(name = "total_calories_burned")
    private Integer totalCaloriesBurned;

}

