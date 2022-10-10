package com.shopnow.shopnow.model;


import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;

import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DatosVendedor {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(updatable = false)
    private Integer id;

    @Unique
    private String nombreEmpresa;

    @Unique
    //@Size(min = 12, max = 12)
    private String RUT;

    @Unique
   //@Size(min = 8)
    private String telefonoEmpresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estadoSolicitud;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Map<Integer,Direccion> locales  = new HashMap<>();

}
