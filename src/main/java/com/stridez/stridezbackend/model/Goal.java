package com.stridez.stridezbackend.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "goals")
@Data
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Integer goalId;

    // Foreign Key ke tabel users
    @JsonProperty("user_id")
    @Column(name = "user_id")
    private String userId;

    @JsonProperty("goal_type")
    @Column(name = "goal_type", length = 20, nullable = false)
    private String goalType;

    @JsonProperty("time_period")
    @Column(name = "time_period", length = 20, nullable = false)
    private String timePeriod;

    @JsonProperty("target_value")
    @Column(name = "target_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal targetValue;

    @Column(name = "level", length = 20)
    private String level;

    // Optional snapshot of user profile at time of creating the goal
    @JsonProperty("gender")
    @Column(name = "gender", length = 10)
    private String gender;

    @JsonProperty("weight_kg")
    @Column(name = "weight_kg", precision = 10, scale = 2)
    private java.math.BigDecimal weightKg;

    @JsonProperty("height_cm")
    @Column(name = "height_cm")
    private Integer heightCm;

    @JsonProperty("target_weight_kg")
    @Column(name = "target_weight_kg", precision = 10, scale = 2)
    private java.math.BigDecimal targetWeightKg;

    @JsonProperty("age")
    @Column(name = "age")
    private Integer age;

    // Representasi boolean yang kompatibel dengan H2 and MySQL
    @Column(name = "is_active")
    @JsonProperty("is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}
