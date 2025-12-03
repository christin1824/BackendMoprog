package com.stridez.stridezbackend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.stridez.stridezbackend.model.RunWaypoint; 
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.math.RoundingMode;
// import com.stridez.stridezbackend.util.LenientBigDecimalDeserializer; // Asumsi import LenientBigDecimalDeserializer

@Data
public class RunWaypointRequest {

    private Integer sequence;

    // Koordinat Latitude
    @JsonProperty("latitude")
    @JsonAlias({"lat", "Lat", "latitude_deg"})
    @JsonDeserialize(using = LenientBigDecimalDeserializer.class) // Asumsi Deserializer
    private BigDecimal latitude;

    // Koordinat Longitude
    @JsonProperty("longitude")
    @JsonAlias({"lng", "lon", "Lon", "longitude_deg"})
    @JsonDeserialize(using = LenientBigDecimalDeserializer.class) // Asumsi Deserializer
    private BigDecimal longitude;

    // Field Sensor dan Kecepatan
    private BigDecimal speedMps;
    private BigDecimal altitudeMeters;
    private BigDecimal accelX;
    private BigDecimal gyroZ;

    // Timestamp
    private LocalDateTime timestamp;

    /**
     * Mengkonversi DTO ini ke RunWaypoint Entity.
     * Menggunakan nilai default (0 atau 0.0) untuk semua field yang bisa null.
     */
    public RunWaypoint toEntity() {

        RunWaypoint w = new RunWaypoint();

        // Nilai BigDecimal default untuk Koordinat (skala 7) dan Sensor (skala 2)
        final BigDecimal ZERO_LAT_LON = BigDecimal.ZERO.setScale(7, RoundingMode.HALF_UP);
        final BigDecimal ZERO_SENSOR = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        // Sequence (Default 0 jika NULL)
        w.setSequence(this.sequence == null ? 0 : this.sequence);

        // Latitude (Wajib. Default 0.0 jika NULL)
        w.setLatitude(this.latitude == null 
                ? ZERO_LAT_LON 
                : this.latitude.setScale(7, RoundingMode.HALF_UP));

        // Longitude (Wajib. Default 0.0 jika NULL)
        w.setLongitude(this.longitude == null 
                ? ZERO_LAT_LON 
                : this.longitude.setScale(7, RoundingMode.HALF_UP));

        // Speed (Default 0.0 jika NULL)
        w.setSpeedMps(this.speedMps == null 
                ? ZERO_SENSOR 
                : this.speedMps.setScale(2, RoundingMode.HALF_UP));

        // Altitude (Default 0.0 jika NULL)
        w.setAltitudeMeters(this.altitudeMeters == null 
                ? ZERO_SENSOR 
                : this.altitudeMeters.setScale(2, RoundingMode.HALF_UP));

        // Accel X (Default 0.0 jika NULL)
        w.setAccelX(this.accelX == null 
                ? ZERO_SENSOR 
                : this.accelX.setScale(2, RoundingMode.HALF_UP));

        // Gyro Z (Default 0.0 jika NULL)
        w.setGyroZ(this.gyroZ == null 
                ? ZERO_SENSOR 
                : this.gyroZ.setScale(2, RoundingMode.HALF_UP));

        // Timestamp (Default waktu saat ini jika NULL)
        w.setTimestamp(this.timestamp == null 
                ? LocalDateTime.now() 
                : this.timestamp);

        return w;
    }
}