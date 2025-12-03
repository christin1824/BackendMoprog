package com.stridez.stridezbackend.service;

import com.stridez.stridezbackend.dto.RunWaypointRequest;
import com.stridez.stridezbackend.model.Run;
import com.stridez.stridezbackend.model.RunWaypoint;
import java.util.List;

public interface RunService {
    // Definisi method utama yang akan dipanggil Controller
    Run saveFullRunData(Run run, List<RunWaypointRequest> waypoints); // ✅ Perubahan di Interface    List<Run> getRunsByUserId(String userId);
    List<Run> getRunsByUserId(String userId);
    List<RunWaypoint> getWaypointsByRunId(Integer runId);
    void recomputeRunMetricsFromWaypoints(Integer runId);
}