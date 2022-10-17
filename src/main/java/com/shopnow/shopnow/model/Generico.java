package com.shopnow.shopnow.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Entity
@DiscriminatorValue("Generico")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Generico extends Usuario {

    @Column(updatable = false) //No puede ser nulleable=false por la estrategia de mapeo de herencia
    private Date fechaNac;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Map<UUID, Reclamo> reclamos = new HashMap<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Map<UUID, Compra> ventas = new HashMap<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Map<UUID, Producto> productos = new HashMap<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Map<UUID, Compra> compras = new HashMap<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Map<UUID, Calificacion> calificaciones = new HashMap<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Map<Integer, Direccion> direccionesEnvio = new HashMap<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Map<String, Tarjeta> tarjetas = new HashMap<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private DatosVendedor datosVendedor = null;

    @Builder.Default
    private String braintreeCustomerId = null;
}
