package com.core.referral.service;

import com.core.referral.dto.response.LinkReferralResponse;
import com.core.referral.dto.response.LinkReferralFullResponse;
import com.core.referral.dto.response.OnboardReferralResponse;
import com.core.referral.dto.response.RegisterReferralCodeResponse;
import com.core.referral.entity.RedReferidos;
import com.core.referral.entity.UsuarioReferidoMap;
import com.core.referral.repository.RedReferidosRepository;
import com.core.referral.repository.UsuarioReferidoMapRepository;
import com.core.referral.sp.ReferralStoredProcedureClient;
import com.core.referral.sp.ReferralStoredProcedureException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ReferralNetworkService {

    private static final Pattern ORACLE_APP_ERROR_PATTERN = Pattern.compile("ORA-(\\d{5})");

    private final UsuarioReferidoMapRepository usuarioReferidoMapRepository;
    private final RedReferidosRepository redReferidosRepository;
    private final ReferralStoredProcedureClient referralStoredProcedureClient;

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
        try {
            referralStoredProcedureClient.spInsertarReferido(referido.getIdReferido(), idReferidor);
            return new LinkReferralResponse(referido.getIdReferido(), idReferidor, true, false);
        } catch (ReferralStoredProcedureException ex) {
            if (ex.isOracleError(20001)) {
                RedReferidos existingRelation = redReferidosRepository.findById(idReferido).orElse(null);
                if (existingRelation != null && existingRelation.getIdReferidor().equals(idReferidor)) {
                    return new LinkReferralResponse(idReferido, idReferidor, true, true);
                }
                throw new IllegalStateException("El usuario referido ya esta ligado a otro referidor");
            }
            if (ex.isOracleError(20002)) {
                throw new IllegalArgumentException("Un usuario no puede referirse a si mismo");
            }
            if (ex.isOracleError(20003)) {
                throw new IllegalStateException("Se detecto un ciclo en la red de referidos");
            }
            if (ex.isOracleError(20004)) {
                throw new IllegalStateException("Se excede el maximo de 3 niveles en la red");
            }
            throw new IllegalStateException("Error al insertar referido en base mediante SP", ex);
        }
    }

    @Transactional
    public LinkReferralFullResponse linkReferralFull(String codigoReferido, Long idReferidor) {
        if (idReferidor == null || idReferidor <= 0) {
            throw new IllegalArgumentException("idReferidor es obligatorio");
        }

        String normalizedCode = normalizeCode(codigoReferido);
        try {
            referralStoredProcedureClient.spInsertarReferidoFull(normalizedCode, idReferidor);
        } catch (ReferralStoredProcedureException ex) {
            if (ex.isOracleError(20001)) {
                throw new IllegalStateException("El usuario ya existe en la red");
            }
            if (ex.isOracleError(20002)) {
                throw new IllegalArgumentException("Un usuario no puede referirse a si mismo");
            }
            if (ex.isOracleError(20003)) {
                throw new IllegalStateException("Se detecto un ciclo en la red de referidos");
            }
            if (ex.isOracleError(20004)) {
                throw new IllegalStateException("Se excede el maximo de 3 niveles en la red");
            }
            String detail = ex.getMessage() != null ? ex.getMessage() : "Error al insertar referido en base mediante SP";
            throw new IllegalStateException(detail, ex);
        }

        UsuarioReferidoMap referido = usuarioReferidoMapRepository.findByCodigoReferidoNormalized(normalizedCode)
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontro el mapping del codigo tras ejecutar sp_insertar_referido_full"
                ));

        return new LinkReferralFullResponse(
                normalizedCode,
                referido.getIdReferido(),
                idReferidor,
                "Referido insertado correctamente",
                true
        );
    }

    private String normalizeCode(String codigoReferido) {
        if (codigoReferido == null || codigoReferido.trim().isEmpty()) {
            throw new IllegalArgumentException("El codigoReferido es obligatorio");
        }
        return codigoReferido.trim().toUpperCase();
    }

    private Integer extractOracleCode(String message) {
        if (message == null) {
            return null;
        }
        Matcher matcher = ORACLE_APP_ERROR_PATTERN.matcher(message);
        if (!matcher.find()) {
            return null;
        }
        return Integer.parseInt(matcher.group(1));
    }
}
