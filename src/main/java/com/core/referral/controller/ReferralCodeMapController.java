package com.core.referral.controller;

import com.core.common.dto.ApiResponse;
import com.core.referral.dto.request.VerifyReferralCodeRequest;
import com.core.referral.dto.response.VerifyReferralCodeResponse;
import com.core.referral.service.ReferralCodeMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/core/referral-codes", "/api/core/referrals/code"})
@RequiredArgsConstructor
public class ReferralCodeMapController {

    private final ReferralCodeMapService referralCodeMapService;

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<VerifyReferralCodeResponse>> verifyReferralCode(
            @RequestBody VerifyReferralCodeRequest request) {

        if (request == null || !StringUtils.hasText(request.codigoReferido())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("El campo codigoReferido es obligatorio", "VALIDACION_FALLIDA"));
        }

        VerifyReferralCodeResponse response;
        try {
            response = referralCodeMapService.verifyCode(request.codigoReferido());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error(ex.getMessage(), "INTEGRACION_AUTH_ERROR"));
        }

        if (!response.existeEnAuth() || !response.existeEnCore()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("El codigo de referido debe existir en AUTH y CORE", "CODIGO_REFERIDO_NO_EXISTE_EN_AMBAS"));
        }

        if (!response.consistente()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("El codigo existe pero no esta sincronizado entre AUTH y CORE", "CODIGO_REFERIDO_DESINCRONIZADO"));
        }

        return ResponseEntity.ok(ApiResponse.success("Consulta de codigo procesada", response));
    }

}
