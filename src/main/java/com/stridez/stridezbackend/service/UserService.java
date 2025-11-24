package com.stridez.stridezbackend.service;

import com.stridez.stridezbackend.model.User;

public interface UserService {
    User save(User user);
    User updateProfile(String userId, User updatedDetails);
    User findByUserId(String userId);
    // [TAMBAHAN] Mendukung pengecekan duplikasi nomor telepon
    User findByPhone(String phone);
    User findByEmail(String email);
}