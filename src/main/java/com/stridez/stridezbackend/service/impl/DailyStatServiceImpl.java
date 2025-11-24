package com.stridez.stridezbackend.service.impl;

import com.stridez.stridezbackend.model.DailyStat;
import com.stridez.stridezbackend.repository.DailyStatRepository;
import com.stridez.stridezbackend.service.DailyStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class DailyStatServiceImpl implements DailyStatService {

    @Autowired
    private DailyStatRepository dailyStatRepository;

    @Transactional
    @Override
    public DailyStat saveOrUpdate(DailyStat newStat) {
        // Logika Bisnis: Cek apakah data untuk tanggal tersebut sudah ada
        Optional<DailyStat> existingStat = dailyStatRepository.findByUserIdAndDate(newStat.getUserId(), newStat.getDate());

        if (existingStat.isPresent()) {
            // UPDATE: Jika sudah ada, perbarui datanya
            DailyStat statToUpdate = existingStat.get();
            statToUpdate.setTotalSteps(newStat.getTotalSteps());
            statToUpdate.setTotalDistanceRun(newStat.getTotalDistanceRun());
            statToUpdate.setCaloriesBurnedRun(newStat.getCaloriesBurnedRun());
            statToUpdate.setCaloriesBurnedSteps(newStat.getCaloriesBurnedSteps());
            return dailyStatRepository.save(statToUpdate);
        } else {
            // CREATE: Jika belum ada, buat entry baru
            return dailyStatRepository.save(newStat);
        }
    }

    @Override
    public Optional<DailyStat> getDailyStat(String userId, LocalDate date) {
        return dailyStatRepository.findByUserIdAndDate(userId, date);
    }
}