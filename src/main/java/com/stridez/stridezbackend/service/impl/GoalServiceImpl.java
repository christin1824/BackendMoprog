package com.stridez.stridezbackend.service.impl;

import com.stridez.stridezbackend.model.Goal;
import com.stridez.stridezbackend.repository.GoalRepository;
import com.stridez.stridezbackend.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GoalServiceImpl implements GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Override
    public Goal createGoal(Goal goal) {
        // Logika Bisnis: Jika user membuat goal baru dengan goal_type yang sama,
        // kita bisa menonaktifkan goal lama yang masih aktif (opsional)
        
        // 1. (Opsional Lanjutan): Nonaktifkan goal aktif sebelumnya
        // getActiveGoalsByUserId(goal.getUserId()).forEach(g -> {
        //     if (g.getGoalType().equals(goal.getGoalType())) {
        //         g.setIsActive(false);
        //         goalRepository.save(g);
        //     }
        // });

        // 2. Set goal baru menjadi aktif
        if (goal.getIsActive() == null) {
            goal.setIsActive(true);
        }
        return goalRepository.save(goal);
    }

    @Override
    public List<Goal> getActiveGoalsByUserId(String userId) {
        return goalRepository.findByUserIdAndIsActive(userId, true);
    }

    @Override
    public Optional<Goal> deactivateGoal(Integer goalId) {
        Optional<Goal> optionalGoal = goalRepository.findById(goalId);
        if (optionalGoal.isPresent()) {
            Goal goal = optionalGoal.get();
            goal.setIsActive(false);
            return Optional.of(goalRepository.save(goal));
        }
        return Optional.empty();
    }
}