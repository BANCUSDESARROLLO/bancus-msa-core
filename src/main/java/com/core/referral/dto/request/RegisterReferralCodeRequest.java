package com.core.referral.dto.request;

public record RegisterReferralCodeRequest(
        String codigoReferido,
        Long idReferido
) {
}
