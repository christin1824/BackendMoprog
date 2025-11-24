package com.stridez.stridezbackend.service.impl;

import com.stridez.stridezbackend.model.User;
import com.stridez.stridezbackend.repository.UserRepository;
import com.stridez.stridezbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User save(User user) {
        // Logika sebelum simpan (dasar)
        return userRepository.save(user);
    }

    @Override
    public User findByUserId(String userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    // [IMPLEMENTASI BARU] Mendukung pencarian berdasarkan nomor telepon
    @Override
    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone); 
    }

    @Override
    public User updateProfile(String userId, User userDetails) {
        // Logika Bisnis: Memastikan user ada sebelum update
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            
            // --- HANYA PERBARUI KOLOM YANG TERSISA DI TABEL USERS ---
            
            // 1. Update Nama
            if (userDetails.getName() != null) {
                existingUser.setName(userDetails.getName());
            }
            
            // 2. Update Nomor Telepon (Kolom Baru)
            if (userDetails.getPhone() != null) {
                // Catatan: Cek duplikasi UNIQUE KEY harusnya dilakukan di Controller
                existingUser.setPhone(userDetails.getPhone()); 
            }

            /* * HAPUS LOGIKA LAMA (KOMENTARI / HAPUS)
            * Kolom-kolom ini sudah tidak ada di tabel 'users' 
            * (Sudah dipindahkan/dihapus)
            */
            // existingUser.setGender(userDetails.getGender()); 
            // existingUser.setWeightKg(userDetails.getWeightKg()); 
            // existingUser.setHeightCm(userDetails.getHeightCm()); 
            // existingUser.setTargetWeightKg(userDetails.getTargetWeightKg()); 
            // existingUser.setAge(userDetails.getAge()); 
            
            // Pastikan Anda memanggil save() terakhir kali
            return userRepository.save(existingUser);
        } else {
            return null; 
        }
    }
}