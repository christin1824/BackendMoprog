package com.stridez.stridezbackend.service;

import com.stridez.stridezbackend.model.UserTotal;
import java.util.Optional;

public interface UserTotalService {
    UserTotal save(UserTotal userTotal);
    Optional<UserTotal> findByUserId(String userId);
}