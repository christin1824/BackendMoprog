package com.stridez.stridezbackend.repository;

import com.stridez.stridezbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    // Metode yang sudah ada
    User findByEmail(String email);

    // [TAMBAHAN WAJIB] Metode baru untuk mencari user berdasarkan nomor telepon
    User findByPhone(String phone); 
}