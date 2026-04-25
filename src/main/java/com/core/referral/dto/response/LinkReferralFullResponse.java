package com.core.referral.dto.response;

public record LinkReferralFullResponse(
        String codigoReferido,
        Long idReferido,
        Long idReferidor,
        String mensaje,
        boolean vinculado
) {
}
