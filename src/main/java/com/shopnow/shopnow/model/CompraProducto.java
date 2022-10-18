package com.shopnow.shopnow.model;

import lombok.*;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompraProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Integer id;

    private Date tiempoEstimadoEnvio;

    private Date horarioRetiroLocal;

    @Column(nullable = false, updatable = false)
    private Boolean esEnvio;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Direccion direccionEnvioORetiro;

    @Column(nullable = false, updatable = false)
    private Float precioUnitario;

    @Column(nullable = false, updatable = false)
    private Integer cantidad;

    @Column(nullable = false, updatable = false)
    @Formula("precio_unitario * cantidad")
    private Float precioTotal;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Size(max = 2)
    private List<Calificacion> calificaciones = new ArrayList<>();
}
