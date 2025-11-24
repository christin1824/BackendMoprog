package com.stridez.stridezbackend.service;

import com.stridez.stridezbackend.model.DailyStat;
import java.time.LocalDate;
import java.util.Optional;

public interface DailyStatService {
    DailyStat saveOrUpdate(DailyStat newStat);
    Optional<DailyStat> getDailyStat(String userId, LocalDate date);
}