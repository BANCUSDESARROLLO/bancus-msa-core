package com.core.referral.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "USUARIO_REFERIDO_MAP", schema = "APP_COREFINAN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioReferidoMap {

    @Id
    @Column(name = "CODIGO_REFERIDO", nullable = false, length = 50)
    private String codigoReferido;

    @Column(name = "ID_REFERIDO", nullable = false)
    private Long idReferido;

    @Column(name = "ESTATUS", insertable = false, updatable = false)
    private Integer estatus;

    @Column(name = "FECHA_CREACION", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;
}
