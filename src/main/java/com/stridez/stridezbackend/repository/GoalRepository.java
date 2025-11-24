package com.stridez.stridezbackend.repository;

import com.stridez.stridezbackend.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Integer> {
    // Digunakan untuk menampilkan target yang masih aktif di Achievement Board
    List<Goal> findByUserIdAndIsActive(String userId, Boolean isActive);
}