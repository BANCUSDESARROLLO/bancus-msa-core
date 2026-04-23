package com.core.referral.service;

import com.core.referral.dto.response.VerifyReferralCodeResponse;
import com.core.referral.entity.UsuarioReferidoMap;
import com.core.referral.repository.UsuarioReferidoMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReferralCodeMapService {

    private final UsuarioReferidoMapRepository usuarioReferidoMapRepository;

    public VerifyReferralCodeResponse verifyCode(String codigoReferido) {
        String normalizedCode = codigoReferido.trim();

        return usuarioReferidoMapRepository.findByCodigoReferidoNormalized(normalizedCode)
                .map(this::toFoundResponse)
                .orElseGet(() -> new VerifyReferralCodeResponse(normalizedCode, false, null));
    }

    private VerifyReferralCodeResponse toFoundResponse(UsuarioReferidoMap usuarioReferidoMap) {
        return new VerifyReferralCodeResponse(
                usuarioReferidoMap.getCodigoReferido(),
                true,
                usuarioReferidoMap.getIdReferido()
        );
    }
}
