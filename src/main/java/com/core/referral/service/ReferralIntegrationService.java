package com.core.referral.service;

import com.core.integration.auth.AuthServiceClient;
import com.core.referral.dto.ReferralCodeOwnerDto;
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
