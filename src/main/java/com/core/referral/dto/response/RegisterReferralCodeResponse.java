package com.core.referral.dto.response;

public record RegisterReferralCodeResponse(
        String codigoReferido,
        Long idReferido,
        boolean creado
) {
}
