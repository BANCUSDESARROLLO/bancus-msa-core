package com.core.referral.controller;

import com.core.common.dto.ApiResponse;
import com.core.referral.dto.request.LinkReferralRequest;
import com.core.referral.dto.request.OnboardReferralRequest;
import com.core.referral.dto.request.RegisterReferralCodeRequest;
import com.core.referral.dto.response.LinkReferralResponse;
import com.core.referral.dto.response.OnboardReferralResponse;
import com.core.referral.dto.response.RegisterReferralCodeResponse;
import com.core.referral.service.ReferralNetworkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/core/referral-network")
@RequiredArgsConstructor
public class ReferralNetworkController {

    private final ReferralNetworkService referralNetworkService;

    @PostMapping("/onboard")
    public ResponseEntity<ApiResponse<OnboardReferralResponse>> onboardReferral(
            @RequestBody OnboardReferralRequest request) {
        if (request == null || request.idReferido() == null || request.idReferido() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("idReferido es obligatorio", "VALIDACION_FALLIDA"));
        }

        try {
            OnboardReferralResponse response = referralNetworkService
                    .onboardReferral(
                            request.idReferido(),
                            request.codigoReferidoPropio(),
                            request.codigoReferidorUsado()
                    );
            return ResponseEntity.ok(ApiResponse.success("Onboarding de referidos procesado", response));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage(), "VALIDACION_FALLIDA"));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(ex.getMessage(), "CONFLICTO_REFERIDOS"));
        }
    }

    @PostMapping("/register-code")
    public ResponseEntity<ApiResponse<RegisterReferralCodeResponse>> registerCode(
            @RequestBody RegisterReferralCodeRequest request) {
        if (request == null || request.idReferido() == null || request.idReferido() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("idReferido es obligatorio", "VALIDACION_FALLIDA"));
        }

        try {
            RegisterReferralCodeResponse response = referralNetworkService
                    .registerReferralCode(request.codigoReferido(), request.idReferido());
            return ResponseEntity.ok(ApiResponse.success("Codigo de referido registrado", response));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage(), "VALIDACION_FALLIDA"));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(ex.getMessage(), "CONFLICTO_REFERIDOS"));
        }
    }

    @PostMapping("/link")
    public ResponseEntity<ApiResponse<LinkReferralResponse>> linkReferral(
            @RequestBody LinkReferralRequest request) {
        if (request == null || request.idReferido() == null || request.idReferido() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("idReferido es obligatorio", "VALIDACION_FALLIDA"));
        }

        try {
            LinkReferralResponse response = referralNetworkService
                    .linkReferral(request.idReferido(), request.codigoReferidor());
            return ResponseEntity.ok(ApiResponse.success("Relacion de referido procesada", response));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage(), "VALIDACION_FALLIDA"));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(ex.getMessage(), "CONFLICTO_REFERIDOS"));
        }
    }
}
