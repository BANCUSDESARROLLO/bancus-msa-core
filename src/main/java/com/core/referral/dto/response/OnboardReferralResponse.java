package com.core.referral.dto.response;

public record OnboardReferralResponse(
        RegisterReferralCodeResponse registroCodigo,
        LinkReferralResponse vinculacion
) {
}
