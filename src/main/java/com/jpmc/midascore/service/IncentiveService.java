package com.jpmc.midascore.service;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.foundation.Incentive;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IncentiveService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String incentiveApiUrl = "http://localhost:8080/incentive";

    public Incentive fetchIncentive(Transaction transaction) {
        return restTemplate.postForObject(incentiveApiUrl, transaction, Incentive.class);
    }
}
