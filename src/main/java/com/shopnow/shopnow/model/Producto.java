package com.shopnow.shopnow.model;

import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Producto {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @Column(nullable = false)
    //@Size(min = 5)
    private String nombre;

    @Column(nullable = false)
    private Integer stock;

    @ElementCollection
    //@Size(max=5)
    @Column(nullable = false)
    private List<String> imagenesURL= new ArrayList<>();

    @Column(nullable = false)
    //@Size(min = 20)
    private String descripcion;

    @Column(nullable = false, updatable = false)
    private Date fechaInicio;

    private Date fechaFin;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoProducto estado;

    @Column(nullable = false, scale=2)
    private Float precio;

    @Column(nullable = false)
    private  Integer diasGarantia;

    @Column(nullable = false)
    private Boolean permiteEnvio;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Map<UUID, Reporte> reportes = new HashMap<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Map<UUID, Comentario> comentarios = new HashMap<>();
}
