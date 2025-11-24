package com.stridez.stridezbackend.controller;

import com.stridez.stridezbackend.dto.RunRequest; // Import DTO baru
import com.stridez.stridezbackend.model.Run;
import com.stridez.stridezbackend.service.RunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// ... (imports lainnya)

@RestController
@RequestMapping("/api/runs")
public class RunController {

    @Autowired 
    private RunService runService; 

    // Ganti penerimaan @RequestBody dari Run menjadi RunRequest DTO
    @PostMapping 
    @ResponseStatus(HttpStatus.CREATED)
    public Run saveFullRunData(@RequestBody RunRequest request) { // <--- PERUBAHAN UTAMA
        
        // Panggil Service Layer dengan data yang sudah di-parse
        return runService.saveFullRunData(request.getRun(), request.getWaypoints()); 
    }

    // Endpoint: GET /api/runs/user/{userId} - ambil semua run untuk user
    @GetMapping("/user/{userId}")
    public java.util.List<Run> getRunsByUser(@PathVariable String userId) {
        return runService.getRunsByUserId(userId);
    }

}