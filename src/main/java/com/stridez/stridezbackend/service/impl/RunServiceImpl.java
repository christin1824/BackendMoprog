package com.stridez.stridezbackend.service.impl;

import com.stridez.stridezbackend.dto.RunWaypointRequest;
import com.stridez.stridezbackend.model.DailyStat;
import com.stridez.stridezbackend.model.Run;
import com.stridez.stridezbackend.model.RunWaypoint;
import com.stridez.stridezbackend.model.UserTotal;
import com.stridez.stridezbackend.repository.DailyStatRepository;
import com.stridez.stridezbackend.repository.RunRepository;
import com.stridez.stridezbackend.repository.RunWaypointRepository;
import com.stridez.stridezbackend.repository.UserTotalRepository;
import com.stridez.stridezbackend.service.RunService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class RunServiceImpl implements RunService {

    private static final Logger logger = LoggerFactory.getLogger(RunServiceImpl.class);

    @Autowired private RunRepository runRepository;
    @Autowired private RunWaypointRepository waypointRepository;
    @Autowired private UserTotalRepository userTotalRepository;
    @Autowired private DailyStatRepository dailyStatRepository;

    @Override
    @Transactional
    public Run saveFullRunData(Run runFromRequest, List<RunWaypointRequest> waypointRequests) {

        // Convert waypoint DTO → entity
        List<RunWaypoint> waypoints = (waypointRequests != null && !waypointRequests.isEmpty())
                ? waypointRequests.stream().map(RunWaypointRequest::toEntity).collect(Collectors.toList())
                : new ArrayList<>();

        // Compute metrics if waypoints exist
        if (!waypoints.isEmpty()) {

            // Sort: by sequence OR timestamp
            waypoints.sort((a, b) -> {
                if (a.getSequence() != null && b.getSequence() != null)
                    return a.getSequence().compareTo(b.getSequence());
                if (a.getTimestamp() != null && b.getTimestamp() != null)
                    return a.getTimestamp().compareTo(b.getTimestamp());
                return 0;
            });

            // Total distance
            BigDecimal totalDistance = BigDecimal.ZERO;

            for (int i = 1; i < waypoints.size(); i++) {
                RunWaypoint prev = waypoints.get(i - 1);
                RunWaypoint cur = waypoints.get(i);

                if (prev.getLatitude() != null && prev.getLongitude() != null &&
                        cur.getLatitude() != null && cur.getLongitude() != null) {

                    double d = haversineMeters(
                            prev.getLatitude().doubleValue(),
                            prev.getLongitude().doubleValue(),
                            cur.getLatitude().doubleValue(),
                            cur.getLongitude().doubleValue()
                    );

                    totalDistance = totalDistance.add(BigDecimal.valueOf(d));
                }
            }

            // Build geometry if missing
            if (runFromRequest.getRunGeometry() == null || runFromRequest.getRunGeometry().isEmpty()) {
                String wkt = buildLineStringFromWaypoints(waypoints);
                if (wkt != null) runFromRequest.setRunGeometry(wkt);
            }

            // Start location (auto)
            RunWaypoint firstWp = waypoints.get(0);

            if ((runFromRequest.getStartLocationName() == null || runFromRequest.getStartLocationName().isBlank()) &&
                    firstWp.getLatitude() != null && firstWp.getLongitude() != null) {

                runFromRequest.setStartLocationName(
                        String.format(Locale.US, "%.7f, %.7f",
                                firstWp.getLatitude().doubleValue(),
                                firstWp.getLongitude().doubleValue())
                );
            }

            // Duration
            var firstTs = firstWp.getTimestamp();
            var lastTs = waypoints.get(waypoints.size() - 1).getTimestamp();

            Integer durationSeconds = (firstTs != null && lastTs != null)
                    ? (int) Math.abs(Duration.between(firstTs, lastTs).getSeconds())
                    : null;

            if (runFromRequest.getStartTime() == null && firstTs != null)
                runFromRequest.setStartTime(firstTs);

            if (runFromRequest.getEndTime() == null && lastTs != null)
                runFromRequest.setEndTime(lastTs);

            if (runFromRequest.getDurationSeconds() == null && durationSeconds != null)
                runFromRequest.setDurationSeconds(durationSeconds);

            // Total distance
            if (runFromRequest.getTotalDistanceMeters() == null)
                runFromRequest.setTotalDistanceMeters(totalDistance.setScale(2, BigDecimal.ROUND_HALF_UP));

            // Average speed
            if (runFromRequest.getAvgSpeedKph() == null &&
                runFromRequest.getDurationSeconds() != null &&
                runFromRequest.getDurationSeconds() > 0) {

                double hours = runFromRequest.getDurationSeconds() / 3600.0;
                double km = runFromRequest.getTotalDistanceMeters().doubleValue() / 1000.0;
                double avgKph = hours > 0 ? km / hours : 0.0;

                runFromRequest.setAvgSpeedKph(BigDecimal.valueOf(avgKph).setScale(2, BigDecimal.ROUND_HALF_UP));
            }

            // Calories (auto)
            if (runFromRequest.getCaloriesBurned() == null) {
                double km = runFromRequest.getTotalDistanceMeters().doubleValue() / 1000.0;
                runFromRequest.setCaloriesBurned((int) Math.round(km * 60.0));
            }
        }

        // Save Run
        Run savedRun = runRepository.save(runFromRequest);

        // Save Waypoints
        if (!waypoints.isEmpty()) {
            for (RunWaypoint w : waypoints) {
                w.setRunId(savedRun.getRunId());
            }
            waypointRepository.saveAll(waypoints);
        }

        updateUserTotals(savedRun);
        updateDailyStats(savedRun);

        return savedRun;
    }

    // ----------------------------------
    // Utility Methods
    // ----------------------------------

    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static String buildLineStringFromWaypoints(List<RunWaypoint> waypoints) {
        if (waypoints == null || waypoints.isEmpty()) return null;

        StringBuilder sb = new StringBuilder("LINESTRING(");
        boolean first = true;

        for (RunWaypoint w : waypoints) {
            if (w.getLatitude() == null || w.getLongitude() == null) continue;

            if (!first) sb.append(", ");
            sb.append(String.format(Locale.US, "%.7f %.7f",
                    w.getLongitude().doubleValue(),
                    w.getLatitude().doubleValue()));

            first = false;
        }

        sb.append(")");
        String s = sb.toString();

        return s.equals("LINESTRING()") ? null : s;
    }

    // ----------------------------------
    // Simple Data Getters
    // ----------------------------------

    @Override
    public List<Run> getRunsByUserId(String userId) {
        return runRepository.findByUserId(userId);
    }

    @Override
    public List<RunWaypoint> getWaypointsByRunId(Integer runId) {
        return waypointRepository.findByRunId(runId);
    }

    // ----------------------------------
    // Recompute Existing Run (Rebuild)
    // ----------------------------------

    @Override
    @Transactional
    public void recomputeRunMetricsFromWaypoints(Integer runId) {
        if (runId == null) return;

        List<RunWaypoint> waypoints = waypointRepository.findByRunId(runId);
        if (waypoints == null || waypoints.isEmpty()) return;

        Run run = runRepository.findById(runId).orElse(null);
        if (run == null) return;

        // Sort waypoints
        waypoints.sort((a, b) -> {
            if (a.getSequence() != null && b.getSequence() != null)
                return a.getSequence().compareTo(b.getSequence());
            if (a.getTimestamp() != null && b.getTimestamp() != null)
                return a.getTimestamp().compareTo(b.getTimestamp());
            return 0;
        });

        // Recompute distance
        BigDecimal totalDistance = BigDecimal.ZERO;
        for (int i = 1; i < waypoints.size(); i++) {
            RunWaypoint prev = waypoints.get(i - 1);
            RunWaypoint cur = waypoints.get(i);

            if (prev.getLatitude() != null && prev.getLongitude() != null &&
                    cur.getLatitude() != null && cur.getLongitude() != null) {

                double d = haversineMeters(
                        prev.getLatitude().doubleValue(),
                        prev.getLongitude().doubleValue(),
                        cur.getLatitude().doubleValue(),
                        cur.getLongitude().doubleValue()
                );
                totalDistance = totalDistance.add(BigDecimal.valueOf(d));
            }
        }

        // Recompute duration
        var firstTs = waypoints.get(0).getTimestamp();
        var lastTs = waypoints.get(waypoints.size() - 1).getTimestamp();

        Integer durationSeconds = (firstTs != null && lastTs != null)
                ? (int) Math.abs(Duration.between(firstTs, lastTs).getSeconds())
                : null;

        if (run.getStartTime() == null && firstTs != null) run.setStartTime(firstTs);
        if (run.getEndTime() == null && lastTs != null) run.setEndTime(lastTs);
        if (durationSeconds != null) run.setDurationSeconds(durationSeconds);

        // Update distance
        run.setTotalDistanceMeters(totalDistance.setScale(2, BigDecimal.ROUND_HALF_UP));

        // Avg speed
        if (run.getDurationSeconds() != null && run.getDurationSeconds() > 0) {
            double hours = run.getDurationSeconds() / 3600.0;
            double km = run.getTotalDistanceMeters().doubleValue() / 1000.0;
            double avg = km / hours;

            run.setAvgSpeedKph(BigDecimal.valueOf(avg).setScale(2, BigDecimal.ROUND_HALF_UP));
        }

        // Calories
        if (run.getCaloriesBurned() == null) {
            double km = run.getTotalDistanceMeters().doubleValue() / 1000.0;
            run.setCaloriesBurned((int) Math.round(km * 60.0));
        }

        try {
            runRepository.save(run);
        } catch (Exception e) {
            logger.error("Failed to update Run metrics for runId={}. Error: {}", runId, e.toString(), e);
        }
    }

    // ----------------------------------
    // User Total Updates
    // ----------------------------------

    private void updateUserTotals(Run savedRun) {
        if (savedRun == null || savedRun.getUserId() == null) return;

        String userId = savedRun.getUserId();
        Optional<UserTotal> optional = userTotalRepository.findById(userId);

        BigDecimal dist = savedRun.getTotalDistanceMeters() == null ? BigDecimal.ZERO : savedRun.getTotalDistanceMeters();
        int duration = savedRun.getDurationSeconds() == null ? 0 : savedRun.getDurationSeconds();
        int calories = savedRun.getCaloriesBurned() == null ? 0 : savedRun.getCaloriesBurned();

        if (optional.isPresent()) {
            UserTotal ut = optional.get();

            ut.setTotalRunsCount((ut.getTotalRunsCount() == null ? 0 : ut.getTotalRunsCount()) + 1);
            ut.setTotalDurationSeconds((ut.getTotalDurationSeconds() == null ? 0 : ut.getTotalDurationSeconds()) + duration);
            ut.setTotalDistanceMeters((ut.getTotalDistanceMeters() == null ? BigDecimal.ZERO : ut.getTotalDistanceMeters()).add(dist));
            ut.setTotalCaloriesBurned((ut.getTotalCaloriesBurned() == null ? 0 : ut.getTotalCaloriesBurned()) + calories);

            try {
                userTotalRepository.save(ut);
            } catch (Exception e) {
                logger.error("Failed to update UserTotal for userId={} runId={} Error: {}", userId, savedRun.getRunId(), e);
            }

        } else {
            UserTotal ut = new UserTotal();
            ut.setUserId(userId);
            ut.setTotalRunsCount(1);
            ut.setTotalDurationSeconds(duration);
            ut.setTotalDistanceMeters(dist);
            ut.setTotalCaloriesBurned(calories);

            try {
                userTotalRepository.save(ut);
            } catch (Exception e) {
                logger.error("Failed to create UserTotal for userId={} runId={} Error: {}", userId, savedRun.getRunId(), e);
            }
        }
    }

    // ----------------------------------
    // Daily Stats Updates
    // ----------------------------------

    private void updateDailyStats(Run savedRun) {
        if (savedRun == null || savedRun.getUserId() == null || savedRun.getStartTime() == null) return;

        String userId = savedRun.getUserId();
        LocalDate date = savedRun.getStartTime().toLocalDate();

        Optional<DailyStat> optional = dailyStatRepository.findByUserIdAndDate(userId, date);

        BigDecimal dist = savedRun.getTotalDistanceMeters() == null ? BigDecimal.ZERO : savedRun.getTotalDistanceMeters();
        int calories = savedRun.getCaloriesBurned() == null ? 0 : savedRun.getCaloriesBurned();

        if (optional.isPresent()) {

            DailyStat ds = optional.get();
            ds.setTotalDistanceRun((ds.getTotalDistanceRun() == null ? BigDecimal.ZERO : ds.getTotalDistanceRun()).add(dist));
            ds.setCaloriesBurnedRun((ds.getCaloriesBurnedRun() == null ? 0 : ds.getCaloriesBurnedRun()) + calories);

            try {
                dailyStatRepository.save(ds);
            } catch (Exception e) {
                logger.error("Failed to update DailyStat for userId={} date={} runId={} Error: {}", userId, date, savedRun.getRunId(), e);
            }

        } else {

            DailyStat ds = new DailyStat();
            ds.setUserId(userId);
            ds.setDate(date);
            ds.setTotalDistanceRun(dist);
            ds.setCaloriesBurnedRun(calories);
            ds.setTotalSteps(0);
            ds.setCaloriesBurnedSteps(0);

            try {
                dailyStatRepository.save(ds);
            } catch (Exception e) {
                logger.error("Failed to create DailyStat for userId={} date={} runId={} Error: {}", userId, date, savedRun.getRunId(), e);
            }
        }
    }
}
