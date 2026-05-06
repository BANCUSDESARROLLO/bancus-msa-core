package com.core.referral.dto.response;

import java.time.LocalDateTime;

public record ReferralTreeTableRowResponse(
        Long idReferido,
        String codigoReferido,
        LocalDateTime fechaCreacion,
        String username,
        String usernameReferidor,
        Integer nivel
) {
}
