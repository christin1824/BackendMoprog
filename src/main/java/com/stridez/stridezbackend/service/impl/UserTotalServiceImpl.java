package com.stridez.stridezbackend.service.impl;

import com.stridez.stridezbackend.model.UserTotal;
import com.stridez.stridezbackend.repository.UserTotalRepository;
import com.stridez.stridezbackend.service.UserTotalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserTotalServiceImpl implements UserTotalService {

    @Autowired
    private UserTotalRepository userTotalRepository;

    @Override
    public UserTotal save(UserTotal userTotal) {
        return userTotalRepository.save(userTotal);
    }

    @Override
    public Optional<UserTotal> findByUserId(String userId) {
        return userTotalRepository.findById(userId);
    }
}