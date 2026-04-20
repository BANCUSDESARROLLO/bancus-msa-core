package com.bancup.referral.dto;

public record ReferralCodeOwnerDto(
        Long idUsuario,
        String nombreUsuario,
        String codigoReferido,
        String estatus
) {
}
