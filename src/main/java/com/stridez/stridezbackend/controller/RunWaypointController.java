package com.stridez.stridezbackend.controller;

import com.stridez.stridezbackend.model.RunWaypoint;
import com.stridez.stridezbackend.repository.RunWaypointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/runs/{runId}/waypoints")
public class RunWaypointController {

    private static final Logger log = LoggerFactory.getLogger(RunWaypointController.class);

    @Autowired
    private RunWaypointRepository waypointRepository;

    @Autowired
    private com.stridez.stridezbackend.service.RunService runService;

    // Tambahkan beberapa waypoint ke run yang sudah ada
    @PostMapping
    public org.springframework.http.ResponseEntity<?> addWaypoints(@PathVariable Integer runId, @RequestBody List<com.stridez.stridezbackend.dto.RunWaypointRequest> waypointsReq) {
        if (waypointsReq == null || waypointsReq.isEmpty()) return org.springframework.http.ResponseEntity.ok(java.util.Collections.emptyList());

        // Debug: log received DTOs so we can inspect what Jackson parsed from the incoming JSON
        if (log.isDebugEnabled()) log.debug("Received waypoint DTOs: {}", waypointsReq);

        // Convert DTOs to entities and set runId
        java.util.List<RunWaypoint> entities = new java.util.ArrayList<>();
        for (com.stridez.stridezbackend.dto.RunWaypointRequest r : waypointsReq) {
            RunWaypoint w = r.toEntity();
            w.setRunId(runId);
            entities.add(w);
        }

        // After mapping, log any cases where one coordinate is present but the other is null
        long partialCoords = entities.stream().filter(w -> (w.getLatitude() == null) ^ (w.getLongitude() == null)).count();
        if (partialCoords > 0) {
            log.warn("{} waypoints have only one of latitude/longitude set for runId={}", partialCoords, runId);
        }

        // Log any missing coordinates for debugging
        long missingCoords = entities.stream().filter(w -> w.getLatitude() == null || w.getLongitude() == null).count();
        if (missingCoords > 0) {
            log.warn("Received {} waypoints but {} are missing latitude/longitude for runId={}", entities.size(), missingCoords, runId);
        }

        log.info("Saving {} waypoints for runId={}", entities.size(), runId);
        try {
            List<RunWaypoint> saved = waypointRepository.saveAll(entities);

            // Setelah menyimpan waypoint, recompute run metrics sehingga jika run dibuat dulu lalu waypoint ditambahkan,
            // run fields seperti total_distance, duration, avg_speed, calories, run_geometry, start_location_name akan terisi.
            try {
                runService.recomputeRunMetricsFromWaypoints(runId);
            } catch (Exception e) {
                log.warn("Failed to recompute run metrics after saving waypoints for runId={}: {}", runId, e.toString());
            }

            return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            log.error("Failed to save waypoints for runId={} due to data integrity issue: {}", runId, ex.toString());
            return org.springframework.http.ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid waypoint data: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error when saving waypoints for runId={}: {}", runId, ex.toString(), ex);
            return org.springframework.http.ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save waypoints");
        }
    }

    // Ambil semua waypoint untuk run tertentu
    @GetMapping
    public List<RunWaypoint> getWaypoints(@PathVariable Integer runId) {
        log.debug("Fetching waypoints for runId={}", runId);
        return waypointRepository.findByRunId(runId);
    }
}
