package com.core.referral.dto.response;

import java.util.List;

public record ReferralTreeDiagramResponse(
        Long rootIdSolicitado,
        boolean rootExisteEnRed,
        int totalNodos,
        List<ReferralTreeDiagramNodeResponse> arboles
) {
}
