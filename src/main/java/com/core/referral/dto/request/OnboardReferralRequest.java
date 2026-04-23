package com.core.referral.dto.request;

public record OnboardReferralRequest(
        Long idReferido,
        String codigoReferidoPropio,
        String codigoReferidorUsado
) {
}
