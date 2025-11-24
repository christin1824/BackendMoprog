package com.stridez.stridezbackend.service.impl;

import com.stridez.stridezbackend.model.DailyStat;
import com.stridez.stridezbackend.model.Run;
import com.stridez.stridezbackend.model.RunWaypoint;
import com.stridez.stridezbackend.model.UserTotal;
import com.stridez.stridezbackend.repository.DailyStatRepository;
import com.stridez.stridezbackend.repository.RunRepository;
import com.stridez.stridezbackend.repository.RunWaypointRepository;
import com.stridez.stridezbackend.repository.UserTotalRepository;
import com.stridez.stridezbackend.service.RunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RunServiceImpl implements RunService {

    private static final Logger logger = LoggerFactory.getLogger(RunServiceImpl.class);

    @Autowired
    private RunRepository runRepository;

    @Autowired
    private RunWaypointRepository waypointRepository;

    @Autowired
    private UserTotalRepository userTotalRepository;

    @Autowired
    private DailyStatRepository dailyStatRepository;

    @Override
    @Transactional
    public Run saveFullRunData(Run run, List<RunWaypoint> waypoints) {
        // Jika ada waypoints, hitung nilai turunan (distance/duration/avg speed/calories) bila belum diisi
        if (waypoints != null && !waypoints.isEmpty()) {
            // Pastikan waypoint terurut (sequence naik). Jika sequence tidak diisi, urutkan by timestamp
            waypoints.sort((a, b) -> {
                if (a.getSequence() != null && b.getSequence() != null) return a.getSequence().compareTo(b.getSequence());
                if (a.getTimestamp() != null && b.getTimestamp() != null) return a.getTimestamp().compareTo(b.getTimestamp());
                return 0;
            });

            // Hitung total distance (meter) dari waypoints
            java.math.BigDecimal totalDistance = java.math.BigDecimal.ZERO;
            for (int i = 1; i < waypoints.size(); i++) {
                RunWaypoint prev = waypoints.get(i - 1);
                RunWaypoint cur = waypoints.get(i);
                if (prev.getLatitude() != null && prev.getLongitude() != null && cur.getLatitude() != null && cur.getLongitude() != null) {
                    double d = haversineMeters(prev.getLatitude().doubleValue(), prev.getLongitude().doubleValue(), cur.getLatitude().doubleValue(), cur.getLongitude().doubleValue());
                    totalDistance = totalDistance.add(java.math.BigDecimal.valueOf(d));
                }
            }

            // Build run geometry (WKT LINESTRING) and start location name from first waypoint if missing
            if (run.getRunGeometry() == null || run.getRunGeometry().isEmpty()) {
                String wkt = buildLineStringFromWaypoints(waypoints);
                if (wkt != null && !wkt.isEmpty()) run.setRunGeometry(wkt);
            }
            if ((run.getStartLocationName() == null || run.getStartLocationName().isBlank()) && waypoints.get(0).getLatitude() != null && waypoints.get(0).getLongitude() != null) {
                String loc = String.format(java.util.Locale.US, "%.7f, %.7f", waypoints.get(0).getLatitude().doubleValue(), waypoints.get(0).getLongitude().doubleValue());
                run.setStartLocationName(loc);
            }

            // Hitung duration (detik)
            Integer durationSeconds = null;
            java.time.LocalDateTime firstTs = waypoints.get(0).getTimestamp();
            java.time.LocalDateTime lastTs = waypoints.get(waypoints.size() - 1).getTimestamp();
            if (firstTs != null && lastTs != null) {
                durationSeconds = (int) java.time.Duration.between(firstTs, lastTs).getSeconds();
                if (durationSeconds < 0) durationSeconds = Math.abs(durationSeconds);
            }

            // Isi ke objek run jika null
            if (run.getStartTime() == null && firstTs != null) run.setStartTime(firstTs);
            if (run.getEndTime() == null && lastTs != null) run.setEndTime(lastTs);
            if (run.getDurationSeconds() == null && durationSeconds != null) run.setDurationSeconds(durationSeconds);
            if (run.getTotalDistanceMeters() == null) run.setTotalDistanceMeters(totalDistance.setScale(2, java.math.RoundingMode.HALF_UP));

            // avg speed (kph) = (distance_km) / (hours)
            if (run.getAvgSpeedKph() == null && run.getDurationSeconds() != null && run.getDurationSeconds() > 0) {
                double hours = run.getDurationSeconds() / 3600.0;
                double km = run.getTotalDistanceMeters().doubleValue() / 1000.0;
                double avgKph = hours > 0 ? (km / hours) : 0.0;
                run.setAvgSpeedKph(java.math.BigDecimal.valueOf(avgKph).setScale(2, java.math.RoundingMode.HALF_UP));
            }

            // Simple calories estimation if not provided: ~60 kcal per km (approximation)
            if (run.getCaloriesBurned() == null) {
                double km = run.getTotalDistanceMeters().doubleValue() / 1000.0;
                int calories = (int) Math.round(km * 60.0);
                run.setCaloriesBurned(calories);
            }
        }

        // 1. Simpan Run utama
        Run savedRun = runRepository.save(run);

        // 2. Simpan Waypoints (set runId ke tiap waypoint)
        if (waypoints != null && !waypoints.isEmpty()) {
            for (RunWaypoint waypoint : waypoints) {
                waypoint.setRunId(savedRun.getRunId()); // PENTING: ID RUN BARU
            }
            waypointRepository.saveAll(waypoints);
        }

        // 3. Update Statistik Kumulatif (user_totals)
        updateUserTotals(savedRun);

        // 4. Update Statistik Harian (daily_stats)
        updateDailyStats(savedRun);

        return savedRun;
    }

    // Helper: compute distance between two lat/lon points in meters (Haversine)
    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth radius in meters
        double latRad1 = Math.toRadians(lat1);
        double latRad2 = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(latRad1) * Math.cos(latRad2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Build a simple WKT LINESTRING from the waypoints (lon lat pairs)
    private static String buildLineStringFromWaypoints(java.util.List<RunWaypoint> waypoints) {
        if (waypoints == null || waypoints.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        sb.append("LINESTRING(");
        boolean first = true;
        for (RunWaypoint w : waypoints) {
            if (w.getLatitude() == null || w.getLongitude() == null) continue;
            if (!first) sb.append(", ");
            // WKT expects lon lat order
            sb.append(String.format(java.util.Locale.US, "%.7f %.7f", w.getLongitude().doubleValue(), w.getLatitude().doubleValue()));
            first = false;
        }
        sb.append(")");
        String s = sb.toString();
        // If only one point or none, return null
        if (s.equals("LINESTRING()") || s.equals("LINESTRING( )")) return null;
        return s;
    }

    @Override
    public List<Run> getRunsByUserId(String userId) {
        return runRepository.findByUserId(userId);
    }

    @Override
    public List<RunWaypoint> getWaypointsByRunId(Integer runId) {
        return waypointRepository.findByRunId(runId);
    }

    @Override
    @Transactional
    public void recomputeRunMetricsFromWaypoints(Integer runId) {
        if (runId == null) return;
        List<RunWaypoint> waypoints = waypointRepository.findByRunId(runId);
        if (waypoints == null || waypoints.isEmpty()) return;

        // Build a temporary Run object to reuse the same computation logic
        Run run = runRepository.findById(runId).orElse(null);
        if (run == null) return;

        // Sort and compute
        waypoints.sort((a, b) -> {
            if (a.getSequence() != null && b.getSequence() != null) return a.getSequence().compareTo(b.getSequence());
            if (a.getTimestamp() != null && b.getTimestamp() != null) return a.getTimestamp().compareTo(b.getTimestamp());
            return 0;
        });

        java.math.BigDecimal totalDistance = java.math.BigDecimal.ZERO;
        for (int i = 1; i < waypoints.size(); i++) {
            RunWaypoint prev = waypoints.get(i - 1);
            RunWaypoint cur = waypoints.get(i);
            if (prev.getLatitude() != null && prev.getLongitude() != null && cur.getLatitude() != null && cur.getLongitude() != null) {
                double d = haversineMeters(prev.getLatitude().doubleValue(), prev.getLongitude().doubleValue(), cur.getLatitude().doubleValue(), cur.getLongitude().doubleValue());
                totalDistance = totalDistance.add(java.math.BigDecimal.valueOf(d));
            }
        }

    java.time.LocalDateTime firstTs = waypoints.get(0).getTimestamp();
    java.time.LocalDateTime lastTs = waypoints.get(waypoints.size() - 1).getTimestamp();
        Integer durationSeconds = null;
        if (firstTs != null && lastTs != null) {
            durationSeconds = (int) java.time.Duration.between(firstTs, lastTs).getSeconds();
            if (durationSeconds < 0) durationSeconds = Math.abs(durationSeconds);
        }

    if (run.getStartTime() == null && firstTs != null) run.setStartTime(firstTs);
    if (run.getEndTime() == null && lastTs != null) run.setEndTime(lastTs);
        if (run.getDurationSeconds() == null && durationSeconds != null) run.setDurationSeconds(durationSeconds);
        run.setTotalDistanceMeters(totalDistance.setScale(2, java.math.RoundingMode.HALF_UP));

        if (run.getAvgSpeedKph() == null && run.getDurationSeconds() != null && run.getDurationSeconds() > 0) {
            double hours = run.getDurationSeconds() / 3600.0;
            double km = run.getTotalDistanceMeters().doubleValue() / 1000.0;
            double avgKph = hours > 0 ? (km / hours) : 0.0;
            run.setAvgSpeedKph(java.math.BigDecimal.valueOf(avgKph).setScale(2, java.math.RoundingMode.HALF_UP));
        }

        if (run.getCaloriesBurned() == null) {
            double km = run.getTotalDistanceMeters().doubleValue() / 1000.0;
            int calories = (int) Math.round(km * 60.0);
            run.setCaloriesBurned(calories);
        }

        try {
            runRepository.save(run);
        } catch (Exception e) {
            logger.error("Failed to update Run metrics for runId={}. Error: {}", runId, e.toString(), e);
        }
    }

    // Helper: update or create UserTotal based on this run
    private void updateUserTotals(Run savedRun) {
        if (savedRun == null || savedRun.getUserId() == null) return;

        String userId = savedRun.getUserId();
        Optional<UserTotal> optional = userTotalRepository.findById(userId);

        BigDecimal runDistance = savedRun.getTotalDistanceMeters() != null ? savedRun.getTotalDistanceMeters() : BigDecimal.ZERO;
        Integer runDuration = savedRun.getDurationSeconds() != null ? savedRun.getDurationSeconds() : 0;
        Integer runCalories = savedRun.getCaloriesBurned() != null ? savedRun.getCaloriesBurned() : 0;

            if (optional.isPresent()) {
            UserTotal ut = optional.get();
            ut.setTotalRunsCount((ut.getTotalRunsCount() == null ? 0 : ut.getTotalRunsCount()) + 1);
            ut.setTotalDurationSeconds((ut.getTotalDurationSeconds() == null ? 0 : ut.getTotalDurationSeconds()) + runDuration);
            ut.setTotalDistanceMeters((ut.getTotalDistanceMeters() == null ? BigDecimal.ZERO : ut.getTotalDistanceMeters()).add(runDistance));
            ut.setTotalCaloriesBurned((ut.getTotalCaloriesBurned() == null ? 0 : ut.getTotalCaloriesBurned()) + runCalories);
            try {
                userTotalRepository.save(ut);
            } catch (Exception e) {
                logger.error("Failed to save/update UserTotal for userId={}. RunId={}. Error: {}", userId, savedRun.getRunId(), e.toString(), e);
            }
        } else {
        // KODE BARU: Hanya perlu set userId. Kolom lain akan default ke 0 (berkat @ColumnDefault)
        UserTotal ut = new UserTotal();
        ut.setUserId(userId);
        ut.setTotalRunsCount(0); // Set awal 0
        ut.setTotalDurationSeconds(0); // Set awal 0
        ut.setTotalDistanceMeters(BigDecimal.ZERO); // Set awal 0
        ut.setTotalCaloriesBurned(0); // Set awal 0

        // Kemudian lanjutkan dengan penjumlahan (untuk menghitung lari pertama)
        ut.setTotalRunsCount(ut.getTotalRunsCount() + 1);
        ut.setTotalDurationSeconds(ut.getTotalDurationSeconds() + runDuration);
        ut.setTotalDistanceMeters(ut.getTotalDistanceMeters().add(runDistance));
        ut.setTotalCaloriesBurned(ut.getTotalCaloriesBurned() + runCalories);
    
        try {
            userTotalRepository.save(ut);
        } catch (Exception e) {
            logger.error("Failed to create UserTotal for userId={}. RunId={}. Error: {}", userId, savedRun.getRunId(), e.toString(), e);
        }
        }
    }

    // Helper: update or create DailyStat for the run's start date
    private void updateDailyStats(Run savedRun) {
        if (savedRun == null || savedRun.getUserId() == null || savedRun.getStartTime() == null) return;

        String userId = savedRun.getUserId();
        LocalDate date = savedRun.getStartTime().toLocalDate();

        Optional<DailyStat> optional = dailyStatRepository.findByUserIdAndDate(userId, date);

        BigDecimal runDistance = savedRun.getTotalDistanceMeters() != null ? savedRun.getTotalDistanceMeters() : BigDecimal.ZERO;
        Integer runCalories = savedRun.getCaloriesBurned() != null ? savedRun.getCaloriesBurned() : 0;

        if (optional.isPresent()) {
            DailyStat ds = optional.get();
            ds.setTotalDistanceRun((ds.getTotalDistanceRun() == null ? BigDecimal.ZERO : ds.getTotalDistanceRun()).add(runDistance));
            ds.setCaloriesBurnedRun((ds.getCaloriesBurnedRun() == null ? 0 : ds.getCaloriesBurnedRun()) + runCalories);
            try {
                dailyStatRepository.save(ds);
            } catch (Exception e) {
                logger.error("Failed to update DailyStat for userId={} date={}. RunId={}. Error: {}", userId, date, savedRun.getRunId(), e.toString(), e);
            }
        } else {
            DailyStat ds = new DailyStat();
            ds.setUserId(userId);
            ds.setDate(date);
            ds.setTotalDistanceRun(runDistance);
            ds.setCaloriesBurnedRun(runCalories);
            ds.setTotalSteps(0);
            ds.setCaloriesBurnedSteps(0);
            try {
                dailyStatRepository.save(ds);
            } catch (Exception e) {
                logger.error("Failed to create DailyStat for userId={} date={}. RunId={}. Error: {}", userId, date, savedRun.getRunId(), e.toString(), e);
            }
        }
    }
}