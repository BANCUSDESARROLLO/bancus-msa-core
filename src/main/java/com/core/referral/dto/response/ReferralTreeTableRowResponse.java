package com.core.referral.dto.response;

import java.time.LocalDateTime;

public record ReferralTreeTableRowResponse(
        Long idReferido,
        Long idReferidor,
        String codigoReferido,
        String codigoReferidor,
        Integer nivel,
        String rutaIds,
        String rutaCodigos,
        LocalDateTime fechaRegistro
) {
}
