package com.bancup.referral.controller;

import com.bancup.referral.dto.ReferralCodeOwnerDto;
import com.bancup.referral.service.ReferralIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/core/referrals")
@RequiredArgsConstructor
public class ReferralIntegrationController {

    private final ReferralIntegrationService referralIntegrationService;

    @GetMapping("/resolve/{code}")
    public ResponseEntity<ReferralCodeOwnerDto> resolveCode(@PathVariable String code) {
        return ResponseEntity.ok(referralIntegrationService.resolveReferralCode(code));
    }
}
