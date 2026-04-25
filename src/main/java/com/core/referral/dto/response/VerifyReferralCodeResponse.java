package com.core.referral.dto.response;

public record VerifyReferralCodeResponse(
        String codigoReferido,
        boolean existe,
        Long idReferido,
        boolean existeEnAuth,
        boolean existeEnCore,
        boolean consistente,
        Long idUsuarioAuth,
        Long idReferidoCore,
        String estatusAuth,
        Integer estatusCore
) {
}
