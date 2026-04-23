package com.core.referral.dto.response;

public record VerifyReferralCodeResponse(
        String codigoReferido,
        boolean existe,
        Long idReferido
) {
}
