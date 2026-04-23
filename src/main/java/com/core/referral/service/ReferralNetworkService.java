package com.core.referral.service;

import com.core.referral.dto.response.LinkReferralResponse;
import com.core.referral.dto.response.OnboardReferralResponse;
import com.core.referral.dto.response.RegisterReferralCodeResponse;
import com.core.referral.entity.RedReferidos;
import com.core.referral.entity.UsuarioReferidoMap;
import com.core.referral.repository.RedReferidosRepository;
import com.core.referral.repository.UsuarioReferidoMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReferralNetworkService {

    private final UsuarioReferidoMapRepository usuarioReferidoMapRepository;
    private final RedReferidosRepository redReferidosRepository;

    @Transactional
    public OnboardReferralResponse onboardReferral(Long idReferido, String codigoReferidoPropio, String codigoReferidorUsado) {
        RegisterReferralCodeResponse registro = registerReferralCode(codigoReferidoPropio, idReferido);

        LinkReferralResponse vinculacion = null;
        if (codigoReferidorUsado != null && !codigoReferidorUsado.trim().isEmpty()) {
            vinculacion = linkReferral(idReferido, codigoReferidorUsado);
        }

        return new OnboardReferralResponse(registro, vinculacion);
    }

    @Transactional
    public RegisterReferralCodeResponse registerReferralCode(String codigoReferido, Long idReferido) {
        String normalizedCode = normalizeCode(codigoReferido);

        UsuarioReferidoMap existingByCode = usuarioReferidoMapRepository
                .findByCodigoReferidoNormalized(normalizedCode)
                .orElse(null);
        if (existingByCode != null) {
            if (existingByCode.getIdReferido().equals(idReferido)) {
                return new RegisterReferralCodeResponse(existingByCode.getCodigoReferido(), idReferido, false);
            }
            throw new IllegalStateException("El codigo de referido ya pertenece a otro usuario");
        }

        UsuarioReferidoMap existingByUser = usuarioReferidoMapRepository.findByIdReferido(idReferido).orElse(null);
        if (existingByUser != null) {
            if (normalizedCode.equalsIgnoreCase(existingByUser.getCodigoReferido())) {
                return new RegisterReferralCodeResponse(existingByUser.getCodigoReferido(), idReferido, false);
            }
            throw new IllegalStateException("El usuario ya tiene otro codigo de referido registrado");
        }

        UsuarioReferidoMap saved = usuarioReferidoMapRepository.save(UsuarioReferidoMap.builder()
                .codigoReferido(normalizedCode)
                .idReferido(idReferido)
                .build());

        return new RegisterReferralCodeResponse(saved.getCodigoReferido(), saved.getIdReferido(), true);
    }

    @Transactional
    public LinkReferralResponse linkReferral(Long idReferido, String codigoReferidor) {
        UsuarioReferidoMap referido = usuarioReferidoMapRepository.findByIdReferido(idReferido)
                .orElseThrow(() -> new IllegalArgumentException("El usuario referido no tiene codigo propio en USUARIO_REFERIDO_MAP"));

        UsuarioReferidoMap referidor = usuarioReferidoMapRepository.findByCodigoReferidoNormalized(normalizeCode(codigoReferidor))
                .orElseThrow(() -> new IllegalArgumentException("El codigo del referidor no existe"));

        Long idReferidor = referidor.getIdReferido();
        if (idReferidor.equals(idReferido)) {
            throw new IllegalArgumentException("Un usuario no puede referirse a si mismo");
        }

        RedReferidos existingRelation = redReferidosRepository.findById(idReferido).orElse(null);
        if (existingRelation != null) {
            if (existingRelation.getIdReferidor().equals(idReferidor)) {
                return new LinkReferralResponse(idReferido, idReferidor, true, true);
            }
            throw new IllegalStateException("El usuario referido ya esta ligado a otro referidor");
        }

        RedReferidos saved = redReferidosRepository.save(RedReferidos.builder()
                .idReferido(referido.getIdReferido())
                .idReferidor(idReferidor)
                .build());

        return new LinkReferralResponse(saved.getIdReferido(), saved.getIdReferidor(), true, false);
    }

    private String normalizeCode(String codigoReferido) {
        if (codigoReferido == null || codigoReferido.trim().isEmpty()) {
            throw new IllegalArgumentException("El codigoReferido es obligatorio");
        }
        return codigoReferido.trim().toUpperCase();
    }
}
