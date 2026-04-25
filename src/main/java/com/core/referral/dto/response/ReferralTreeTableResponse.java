package com.core.referral.dto.response;

import java.util.List;

public record ReferralTreeTableResponse(
        Long rootIdSolicitado,
        boolean rootExisteEnRed,
        int totalRegistros,
        List<ReferralTreeTableRowResponse> registros
) {
}
