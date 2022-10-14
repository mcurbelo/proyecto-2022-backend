package com.shopnow.shopnow.model;


import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DatosVendedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Integer id;

    private String nombreEmpresa;

    //@Size(min = 12, max = 12)
    private String rut;

    //@Size(min = 8)
    private String telefonoEmpresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estadoSolicitud;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Map<Integer, Direccion> locales = new HashMap<>();
}
