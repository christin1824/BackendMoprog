package com.stridez.stridezbackend.repository;

import com.stridez.stridezbackend.model.UserTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTotalRepository extends JpaRepository<UserTotal, String> {
    // Tipe ID adalah String karena Primary Key-nya adalah user_id (String)
}