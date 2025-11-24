package com.stridez.stridezbackend.controller;

import com.stridez.stridezbackend.model.User;
import com.stridez.stridezbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

@RestController
@RequestMapping({"/api/users", "/users"})
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    // ====================================================================
    // 1. POST: Pendaftaran / User Creation (Sign Up)
    // ====================================================================
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createUser(@RequestBody User user, HttpServletRequest request) {
        log.info("Received createUser request: {}", user);

        // If an Authorization: Bearer <idToken> header is present and Firebase is initialized,
        // verify the token and prefer the uid from the token as the canonical userId.
        String auth = request.getHeader("Authorization");
        String uidFromToken = null;
        String tokenEmail = null;
        if (auth != null && auth.startsWith("Bearer ")) {
            String idToken = auth.substring("Bearer ".length()).trim();
            try {
                FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
                uidFromToken = decoded.getUid();
                tokenEmail = decoded.getEmail();
                log.debug("Verified Firebase token for uid={}", uidFromToken);
            } catch (FirebaseAuthException e) {
                log.warn("Invalid Firebase ID token: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired Firebase ID token");
            } catch (IllegalStateException e) {
                // If FirebaseAdmin not initialized, verifyIdToken may throw IllegalStateException
                log.warn("FirebaseAdmin not initialized; falling back to userId in request body.");
            }
        }

        if (uidFromToken != null) {
            // Ensure we use the uid from token as primary key
            user.setUserId(uidFromToken);
            if (user.getEmail() == null && tokenEmail != null) user.setEmail(tokenEmail);
        }

        // If still no userId, require it (or choose to auto-generate). Return 400 if missing.
        if (user.getUserId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("userId is required (or provide a valid Firebase ID token)");
        }

        // Jika user sudah ada, lakukan update (upsert behavior)
        if (userRepository.existsById(user.getUserId())) {
            User existing = userRepository.findById(user.getUserId()).orElse(null);
            if (existing == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User exists but cannot be loaded");
            }

            // Jika email dikirim, pastikan tidak dipakai akun lain
            if (user.getEmail() != null) {
                var byEmail = userRepository.findByEmail(user.getEmail());
                if (byEmail != null && !byEmail.getUserId().equals(user.getUserId())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists.");
                }
                existing.setEmail(user.getEmail());
            }

            if (user.getName() != null) existing.setName(user.getName());
            if (user.getPhone() != null) existing.setPhone(user.getPhone());

            User updatedUser = userRepository.save(existing);
            return ResponseEntity.ok(updatedUser);
        }

        // New user: validate unique email if provided
        if (user.getEmail() != null && userRepository.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists.");
        }

        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    // ====================================================================
    // 2. GET: Ambil Data Profil (Melihat Profil)
    // ====================================================================
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        log.info("getUserById request: {}", userId);
        Optional<User> user = userRepository.findById(userId);
        return user.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ====================================================================
    // 3. PUT: Update Profil (HANYA Update Nama dan Nomor Telepon di tabel users)
    // Note: Data fisik (gender, weight, dll) ada di tabel 'goals' (sesuai arahan Anda)
    // ====================================================================
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable String userId, @RequestBody User userDetails) {
        log.info("updateUser request for {}: {}", userId, userDetails);
        
        return userRepository.findById(userId)
            .map(existingUser -> {
                
                // 1. Update Nama (Jika dikirim)
                if (userDetails.getName() != null) {
                    existingUser.setName(userDetails.getName());
                }
                
                // 2. Update Nomor Telepon (Wajib cek duplikasi jika diubah)
                if (userDetails.getPhone() != null && 
                    !userDetails.getPhone().equals(existingUser.getPhone())) {
                    
                    if (userRepository.findByPhone(userDetails.getPhone()) != null) {
                        // Jika nomor telepon baru sudah dipakai oleh user lain
                         return ResponseEntity.status(HttpStatus.CONFLICT).body(existingUser);
                    }
                    existingUser.setPhone(userDetails.getPhone());
                }

                // Kolom seperti email, joinDate tidak boleh diupdate
                
                User updatedUser = userRepository.save(existingUser);
                return ResponseEntity.ok(updatedUser);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Simple health check for this controller
    @GetMapping("/ping")
    public String ping() {
        return "server-aktif";
    }
}