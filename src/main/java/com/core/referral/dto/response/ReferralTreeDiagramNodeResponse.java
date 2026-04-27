package com.core.referral.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ReferralTreeDiagramNodeResponse(
        Long idReferido,
        Long idReferidor,
        String codigoReferido,
        String codigoReferidor,
        String username,
        String usernameReferidor,
        LocalDateTime fechaRegistro,
        Integer nivel,
        List<ReferralTreeDiagramNodeResponse> referidos
) {
}
