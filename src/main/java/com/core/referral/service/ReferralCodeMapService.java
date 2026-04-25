package com.core.referral.service;

import com.core.exception.AuthIntegrationException;
import com.core.exception.ResourceNotFoundException;
import com.core.integration.auth.AuthServiceClient;
import com.core.referral.dto.ReferralCodeOwnerDto;
import com.core.referral.dto.response.VerifyReferralCodeResponse;
import com.core.referral.entity.UsuarioReferidoMap;
import com.core.referral.repository.UsuarioReferidoMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReferralCodeMapService {

    private final AuthServiceClient authServiceClient;
    private final UsuarioReferidoMapRepository usuarioReferidoMapRepository;

    public VerifyReferralCodeResponse verifyCode(String codigoReferido) {
        String normalizedCode = normalizeCode(codigoReferido);

        ReferralCodeOwnerDto authOwner = null;
        try {
            authOwner = authServiceClient.getOwnerByReferralCode(normalizedCode);
        } catch (ResourceNotFoundException ignored) {
            authOwner = null;
        } catch (AuthIntegrationException ex) {
            throw new IllegalStateException("No fue posible validar el codigo contra AUTH", ex);
        }

        UsuarioReferidoMap coreMap = usuarioReferidoMapRepository
                .findByCodigoReferidoNormalized(normalizedCode)
                .orElse(null);

        boolean existsInAuth = authOwner != null;
        boolean existsInCore = coreMap != null;

        Long authId = existsInAuth ? authOwner.idUsuario() : null;
        Long coreId = existsInCore ? coreMap.getIdReferido() : null;

        boolean consistent = existsInAuth
                && existsInCore
                && authId != null
                && authId.equals(coreId);

        return new VerifyReferralCodeResponse(
                normalizedCode,
                consistent,
                consistent ? authId : null,
                existsInAuth,
                existsInCore,
                consistent,
                authId,
                coreId,
                existsInAuth ? authOwner.estatus() : null,
                existsInCore ? coreMap.getEstatus() : null
        );
    }

    private String normalizeCode(String codigoReferido) {
        return codigoReferido == null ? "" : codigoReferido.trim().toUpperCase();
    }
}
