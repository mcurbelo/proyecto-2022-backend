package com.shopnow.shopnow.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventoPromocional {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @Column(nullable = false, updatable = false)
    private String nombre;

    @Column(nullable = false, updatable = false)
    private Date fechaInicio;

    @Column(nullable = false, updatable = false)
    private Date fechaFin;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Map<UUID,Producto> productos = new HashMap<>();

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Map<String, Categoria> categorias = new HashMap<>();

}
