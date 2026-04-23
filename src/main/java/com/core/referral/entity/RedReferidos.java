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
@Table(name = "RED_REFERIDOS", schema = "APP_COREFINAN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedReferidos {

    @Id
    @Column(name = "ID_REFERIDO", nullable = false)
    private Long idReferido;

    @Column(name = "ID_REFERIDOR", nullable = false)
    private Long idReferidor;

    @Column(name = "FECHA_REGISTRO", insertable = false, updatable = false)
    private LocalDateTime fechaRegistro;
}
