package com.jpmc.midascore.controller;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class BalanceController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/balance")
    public Balance getBalance(@RequestParam("userId") Long userId) {
        UserRecord user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new Balance(0.0f);  // Must return 0 balance for nonexistent user
        }
        return new Balance(user.getBalance());
    }
}
