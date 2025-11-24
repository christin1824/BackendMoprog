package com.stridez.stridezbackend.dto;

import com.stridez.stridezbackend.model.Run;
import com.stridez.stridezbackend.model.RunWaypoint;
import lombok.Data;
import java.util.List;

// DTO ini merepresentasikan persis struktur JSON yang Anda kirim
@Data 
public class RunRequest {
    private Run run; // Objek Run utama
    private List<RunWaypoint> waypoints; // Daftar Waypoints
}
