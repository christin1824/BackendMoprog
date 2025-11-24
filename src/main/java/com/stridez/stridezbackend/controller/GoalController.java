package com.stridez.stridezbackend.controller;

import com.stridez.stridezbackend.model.Goal;
import com.stridez.stridezbackend.repository.GoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    @Autowired
    private GoalRepository goalRepository;

    private static final Logger log = LoggerFactory.getLogger(GoalController.class);

    // POST: Membuat Target Baru (Set Goals)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Goal createGoal(@RequestBody Goal goal) {
        // Log incoming payload to help diagnose if frontend is sending user fields here
        log.info("Received createGoal request: {}", goal);
        if (goal.getIsActive() == null) {
            goal.setIsActive(true);
        }
        return goalRepository.save(goal);
    }

    // GET: Mengambil Target yang Aktif (Achievement Board/Dashboard)
    @GetMapping("/active/{userId}")
    public List<Goal> getActiveGoalsByUserId(@PathVariable String userId) {
        // Asumsi findByUserIdAndIsActive ada di GoalRepository
        return goalRepository.findByUserIdAndIsActive(userId, true); 
    }
    
    // ... Tambahkan endpoint untuk PUT (update) atau DELETE jika diperlukan
}