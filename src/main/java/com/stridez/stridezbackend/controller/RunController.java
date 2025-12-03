package com.stridez.stridezbackend.controller;

import com.stridez.stridezbackend.dto.RunRequest; // Import DTO utama
import com.stridez.stridezbackend.model.Run;
import com.stridez.stridezbackend.model.RunWaypoint;
import com.stridez.stridezbackend.service.RunService;
import com.stridez.stridezbackend.dto.RunWaypointRequest; // Import ini untuk DTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
// import java.util.stream.Collectors; // TIDAK DIBUTUHKAN LAGI

@RestController
@RequestMapping("/api/runs")
public class RunController {

    @Autowired 
    private RunService runService; 

// Ganti penerimaan @RequestBody dari Run menjadi RunRequest DTO
    @PostMapping 
    @ResponseStatus(HttpStatus.CREATED)
    public Run saveFullRunData(@RequestBody RunRequest request) { 
        // NOTE: Menghapus logika konversi DTO ke Entity di sini
        // Service Layer (RunService) sekarang menerima List<RunWaypointRequest>
        
 // Panggil Service Layer dengan Run Entity dan List DTO Waypoint
         return runService.saveFullRunData(request.getRun(), request.getWaypoints()); 
     }

// Endpoint: GET /api/runs/user/{userId} - ambil semua run untuk user
    @GetMapping("/user/{userId}")
    public List<Run> getRunsByUser(@PathVariable String userId) {
         return runService.getRunsByUserId(userId);
     }

}