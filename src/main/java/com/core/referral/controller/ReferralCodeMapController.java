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

        VerifyReferralCodeResponse response = referralCodeMapService.verifyCode(request.codigoReferido());
        if (!response.existe()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("El codigo de referido no existe", "CODIGO_REFERIDO_NO_EXISTE"));
        }

        return ResponseEntity.ok(ApiResponse.success("Consulta de codigo procesada", response));
    }

}
