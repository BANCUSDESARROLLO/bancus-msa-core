package com.core.referral.dto.response;

public record LinkReferralResponse(
        Long idReferido,
        Long idReferidor,
        boolean vinculado,
        boolean yaExistia
) {
}
