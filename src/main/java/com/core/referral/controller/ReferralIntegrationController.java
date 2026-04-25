package com.core.referral.controller;

import com.core.exception.AuthIntegrationException;
import com.core.exception.ResourceNotFoundException;
import com.core.referral.dto.ReferralCodeOwnerDto;
import com.core.referral.service.ReferralIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
        try {
            return ResponseEntity.ok(referralIntegrationService.resolveReferralCode(code));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (AuthIntegrationException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}
