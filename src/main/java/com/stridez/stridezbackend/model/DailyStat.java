package com.stridez.stridezbackend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "daily_stats")
@Data
public class DailyStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_stat_id")
    private Long dailyStatId;

    // --- PERBAIKAN UTAMA: RELASI ANTAR ENTITY ---
    // Diasumsikan 'DailyStat' berelasi dengan Entitas 'User' (misalnya, 'User.java').
    // Menggunakan @ManyToOne dan @JoinColumn lebih baik daripada hanya 'private String userId'.
    // Ini membantu JPA mengelola relasi dan integritas data secara otomatis.
    
    // Asumsi: Ada Entitas User.java dengan Primary Key 'id' bertipe Long/String.
    // Jika userId di database Anda bertipe String dan merupakan FK, pertahankan String.
    
    // Jika Anda ingin tetap menggunakan String:
    @Column(name = "user_id", nullable = false) // Menambahkan nullable = false jika userId wajib ada
    private String userId;
    
    // JIKA Anda memiliki Entitas User, ini adalah cara yang LEBIH BAIK:
    /*
    @ManyToOne(fetch = FetchType.LAZY) // Menggunakan LAZY load untuk performa
    @JoinColumn(name = "user_id", referencedColumnName = "id_user", nullable = false)
    private User user;
    */

    // --- PENYEMPURNAAN LAIN ---

    // date biasanya wajib ada
    @Column(name = "date", nullable = false) 
    private LocalDate date;

    // Untuk angka Integer, jika Anda ingin memastikannya tidak null di database
    @ColumnDefault("0")
    @Column(name = "total_steps", nullable = false) 
    private Integer totalSteps;

    @ColumnDefault("0.00")
    // Pastikan precision dan scale sesuai dengan kebutuhan database Anda
    @Column(name = "total_distance_run", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalDistanceRun;

    @ColumnDefault("0")
    @Column(name = "calories_burned_run", nullable = false)
    private Integer caloriesBurnedRun;

    @ColumnDefault("0")
    @Column(name = "calories_burned_steps", nullable = false)
    private Integer caloriesBurnedSteps;

    // Tambahkan konstruktor jika diperlukan oleh framework atau kebutuhan spesifik, 
    // tetapi Lombok @Data sudah menyediakan getter/setter/toString/equals/hashCode.
}