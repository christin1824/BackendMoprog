package com.stridez.stridezbackend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.math.RoundingMode;

@Data
public class RunWaypointRequest {
    private Integer sequence;

    // Accept multiple incoming names for latitude/longitude
    @JsonProperty("latitude")
    @JsonAlias({"lat", "Lat", "latitude_deg"})
    @JsonDeserialize(using = LenientBigDecimalDeserializer.class)
    private BigDecimal latitude;

    @JsonProperty("longitude")
    @JsonAlias({"lng", "lon", "Lon", "longitude_deg"})
    @JsonDeserialize(using = LenientBigDecimalDeserializer.class)
    private BigDecimal longitude;

    private BigDecimal speedMps;
    private BigDecimal altitudeMeters;
    private BigDecimal accelX;
    private BigDecimal gyroZ;

    // Timestamps: expect ISO local datetime like "2025-11-06T12:00:00"
    private LocalDateTime timestamp;

    // Convert to entity helper (no runId)
    public com.stridez.stridezbackend.model.RunWaypoint toEntity() {
        com.stridez.stridezbackend.model.RunWaypoint w = new com.stridez.stridezbackend.model.RunWaypoint();
        w.setSequence(this.sequence == null ? 0 : this.sequence);
        // Round numeric values to match DB column precision/scale to avoid insert errors
        if (this.latitude != null) {
            w.setLatitude(this.latitude.setScale(7, RoundingMode.HALF_UP));
        }
        if (this.longitude != null) {
            w.setLongitude(this.longitude.setScale(7, RoundingMode.HALF_UP));
        }
        if (this.speedMps != null) {
            w.setSpeedMps(this.speedMps.setScale(2, RoundingMode.HALF_UP));
        }
        if (this.altitudeMeters != null) {
            w.setAltitudeMeters(this.altitudeMeters.setScale(2, RoundingMode.HALF_UP));
        }
        if (this.accelX != null) {
            w.setAccelX(this.accelX.setScale(2, RoundingMode.HALF_UP));
        }
        if (this.gyroZ != null) {
            w.setGyroZ(this.gyroZ.setScale(2, RoundingMode.HALF_UP));
        }

        // If timestamp not provided by client, set to now to satisfy non-null DB constraint
        if (this.timestamp == null) {
            w.setTimestamp(LocalDateTime.now());
        } else {
            w.setTimestamp(this.timestamp);
        }
        return w;
    }
}
