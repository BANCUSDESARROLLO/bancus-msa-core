package com.core.referral.sp;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ReferralStoredProcedureClient {

    private static final Pattern ORACLE_APP_ERROR_PATTERN = Pattern.compile("ORA-(\\d{5})");

    private final JdbcTemplate jdbcTemplate;

    @Value("${core.referral.schema:APP_COREFINAN}")
    private String referralSchema;

    public void spInsertarReferido(Long idReferido, Long idReferidor) {
        String call = "{ call " + referralSchema + ".sp_insertar_referido(?, ?) }";

        try {
            jdbcTemplate.execute(call, (CallableStatement cs) -> {
                cs.setLong(1, idReferido);
                cs.setLong(2, idReferidor);
                cs.execute();
                return null;
            });
        } catch (DataAccessException ex) {
            throw toReferralProcedureException("Error al ejecutar sp_insertar_referido", ex);
        }
    }

    public void spInsertarReferidoFull(String codigoReferido, Long idReferidor) {
        String call = "{ call " + referralSchema + ".sp_insertar_referido_full(?, ?, ?, ?) }";

        try {
            SpInsertarReferidoFullResult result = jdbcTemplate.execute(call, (CallableStatement cs) -> {
                cs.setString(1, codigoReferido);
                cs.setLong(2, idReferidor);
                cs.registerOutParameter(3, Types.NUMERIC);
                cs.registerOutParameter(4, Types.VARCHAR);
                cs.execute();
                Long outIdReferido = cs.getObject(3) != null ? cs.getLong(3) : null;
                String outMensaje = cs.getString(4);
                return new SpInsertarReferidoFullResult(outIdReferido, outMensaje);
            });

            if (result != null && result.mensaje() != null) {
                String mensaje = result.mensaje().trim();
                if (!mensaje.isEmpty() && !"OK".equalsIgnoreCase(mensaje)) {
                    Integer oracleCode = extractOracleCode(new IllegalStateException(mensaje));
                    throw new ReferralStoredProcedureException(
                            "sp_insertar_referido_full reporto error: " + mensaje,
                            oracleCode,
                            null
                    );
                }
            }
        } catch (DataAccessException ex) {
            throw toReferralProcedureException("Error al ejecutar sp_insertar_referido_full", ex);
        }
    }

    private record SpInsertarReferidoFullResult(Long idReferido, String mensaje) {
    }

    private ReferralStoredProcedureException toReferralProcedureException(String message, DataAccessException ex) {
        Integer oracleCode = extractOracleCode(ex);
        return new ReferralStoredProcedureException(message, oracleCode, ex);
    }

    private Integer extractOracleCode(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException sqlEx) {
                int code = sqlEx.getErrorCode();
                if (code != 0) {
                    return code;
                }
            }

            String msg = current.getMessage();
            if (msg != null) {
                Matcher matcher = ORACLE_APP_ERROR_PATTERN.matcher(msg);
                if (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                }
            }
            current = current.getCause();
        }
        return null;
    }

}
