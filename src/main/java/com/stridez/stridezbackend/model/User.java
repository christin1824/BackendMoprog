package com.stridez.stridezbackend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data; 
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "users")
@Data 
public class User {

    @Id
    @Column(name = "user_id", length = 255)
    private String userId; // PRIMARY KEY, UID Firebase

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", unique = true, length = 100)
    private String email;
    
    // --- KOLOM NOMOR TELEPON BARU ---
    @Column(name = "phone", unique = true, length = 20)
    private String phone; 
    // ---------------------------------

    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "join_date", columnDefinition = "DATETIME")
    private LocalDateTime joinDate;

    // Tanggal mulai menjadi member (diset otomatis saat pertama kali dibuat jika null)
    @Column(name = "member_since_date", columnDefinition = "DATETIME")
    private LocalDateTime memberSinceDate;

    // Catatan: Kolom profile (gender, weight, dll) sudah dihapus/dipindahkan

    @PrePersist
    protected void onCreate() {
        if (this.joinDate == null) this.joinDate = LocalDateTime.now();
        if (this.memberSinceDate == null) {
            // Simpan sebagai tanggal hari ini pada jam 00:00:00 untuk konsistensi tampilan
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            this.memberSinceDate = startOfDay;
        }
    }
}