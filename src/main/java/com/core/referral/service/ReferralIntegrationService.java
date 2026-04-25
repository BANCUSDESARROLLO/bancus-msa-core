package com.core.referral.service;

import com.core.exception.ResourceNotFoundException;
import com.core.integration.auth.AuthServiceClient;
import com.core.referral.dto.ReferralCodeOwnerDto;
import com.core.referral.entity.UsuarioReferidoMap;
import com.core.referral.repository.UsuarioReferidoMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReferralIntegrationService {

    private final AuthServiceClient authServiceClient;
    private final UsuarioReferidoMapRepository usuarioReferidoMapRepository;

    public ReferralCodeOwnerDto resolveReferralCode(String code) {
        String normalizedCode = normalizeCode(code);
        ReferralCodeOwnerDto owner = authServiceClient.getOwnerByReferralCode(normalizedCode);

        UsuarioReferidoMap coreMap = usuarioReferidoMapRepository
                .findByCodigoReferidoNormalized(normalizedCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Codigo de referido no encontrado en CORE: " + normalizedCode
                ));

        if (owner.idUsuario() == null || !owner.idUsuario().equals(coreMap.getIdReferido())) {
            throw new IllegalStateException("El codigo existe, pero no coincide entre AUTH y CORE");
        }

        return owner;
    }

    private String normalizeCode(String code) {
        if (code == null) {
            return "";
        }
        return code.trim().toUpperCase();
    }
}
