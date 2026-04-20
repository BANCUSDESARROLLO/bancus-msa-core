package com.bancup.referral.service;

import com.bancup.integration.auth.AuthServiceClient;
import com.bancup.referral.dto.ReferralCodeOwnerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReferralIntegrationService {

    private final AuthServiceClient authServiceClient;

    public ReferralCodeOwnerDto resolveReferralCode(String code) {
        return authServiceClient.getOwnerByReferralCode(code);
    }
}
