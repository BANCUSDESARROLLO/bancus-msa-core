package com.core.referral.repository;

import com.core.referral.entity.UsuarioReferidoMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioReferidoMapRepository extends JpaRepository<UsuarioReferidoMap, String> {

    Optional<UsuarioReferidoMap> findByIdReferido(Long idReferido);

    @Query(value = """
            SELECT CODIGO_REFERIDO, ID_REFERIDO, FECHA_CREACION
            FROM APP_COREFINAN.USUARIO_REFERIDO_MAP
            WHERE REGEXP_REPLACE(UPPER(CODIGO_REFERIDO), '[^A-Z0-9]', '') =
                  REGEXP_REPLACE(UPPER(:codigoReferido), '[^A-Z0-9]', '')
            """, nativeQuery = true)
    Optional<UsuarioReferidoMap> findByCodigoReferidoNormalized(@Param("codigoReferido") String codigoReferido);
}
