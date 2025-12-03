
package com.stridez.stridezbackend.dto;

import com.stridez.stridezbackend.model.Run;
import lombok.Data;
import java.util.List;
// DTO ini merepresentasikan persis struktur JSON yang Anda kirim
@Data 
public class RunRequest {
    private Run run; // Objek Run utama
    private List<RunWaypointRequest> waypoints;// Daftar Waypoints
}
