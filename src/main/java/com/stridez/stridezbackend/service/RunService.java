package com.stridez.stridezbackend.service;

import com.stridez.stridezbackend.model.Run;
import com.stridez.stridezbackend.model.RunWaypoint;
import java.util.List;

public interface RunService {
    // Definisi method utama yang akan dipanggil Controller
    Run saveFullRunData(Run run, List<RunWaypoint> waypoints);
    List<Run> getRunsByUserId(String userId);
    List<RunWaypoint> getWaypointsByRunId(Integer runId);
    // When waypoints are added later, recompute run metrics (distance/duration/avg speed/calories)
    void recomputeRunMetricsFromWaypoints(Integer runId);
}