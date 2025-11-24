package com.stridez.stridezbackend.service;

import com.stridez.stridezbackend.model.Goal;
import java.util.List;
import java.util.Optional;

public interface GoalService {
    Goal createGoal(Goal goal);
    List<Goal> getActiveGoalsByUserId(String userId);
    Optional<Goal> deactivateGoal(Integer goalId);
}